/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME;

public class VendorTask extends AbstractGolangTask {
    public VendorTask() {
        dependsOn(RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    void vendor() {
        File vendorDir = new File(getProject().getRootDir(), VendorDependencyFactory.VENDOR_DIRECTORY);
        if (vendorDir.exists()) {
            Assert.isTrue(vendorDir.isDirectory(), "Vendor must be a directory!");
        } else {
            IOUtils.forceMkdir(vendorDir);
        }

        DependencyTreeNode buildTree = getTask(ResolveBuildDependenciesTask.class).getDependencyTree();
        DependencyTreeNode testTree = getTask(ResolveTestDependenciesTask.class).getDependencyTree();

        GolangDependencySet buildDependencies = buildTree.flatten();
        GolangDependencySet testDependencies = testTree.flatten();
        GolangDependencySet resultDependencies = GolangDependencySet.merge(buildDependencies, testDependencies);

        installRootVendorDependencies(resultDependencies);

        installNonRootVendorDependencies(resultDependencies);

        IOUtils.markAndDelete(vendorDir, dir -> vendorDirShouldBeReserved(dir, vendorDir, resultDependencies));

        deleteFilesUnderVendor(vendorDir);
    }

    private void deleteFilesUnderVendor(File vendorDir) {
        IOUtils.safeListFiles(vendorDir)
                .stream()
                .filter(File::isFile)
                .forEach(IOUtils::forceDelete);
    }

    private void installNonRootVendorDependencies(GolangDependencySet allDependencies) {
        allDependencies
                .stream()
                .filter(d -> !isGogradleVendorDependency(d))
                .forEach(d -> ResolvedDependency.class.cast(d).installTo(targetDir(d)));
    }

    private File targetDir(GolangDependency dependency) {
        return new File(getProject().getRootDir(), "vendor/" + dependency.getName());
    }

    private void installRootVendorDependencies(GolangDependencySet allDependencies) {
        List<VendorResolvedDependency> rootVendorDependencies = allDependencies
                .stream()
                .filter(this::isGogradleVendorDependency)
                .map(d -> (VendorResolvedDependency) d)
                .collect(Collectors.toList());
        rootVendorDependencies.sort((a, b) -> b.getRelativePathToHost().length() - a.getRelativePathToHost().length());
        rootVendorDependencies.forEach(d -> {
            File targetDir = targetDir(d);
            if (isNestedVendorPackage(d)) {
                IOUtils.forceMkdir(targetDir);
                IOUtils.clearDirectory(targetDir);
                d.installTo(targetDir);
            } else {
                // remove nested vendor, i.e. flatten vendors
                IOUtils.forceDelete(new File(targetDir, "vendor"));
            }
        });
    }

    private boolean isNestedVendorPackage(VendorResolvedDependency d) {
        // i.e. vendor/github.com/a/b/vendor/github.com/c/d
        String name = d.getName();
        String vendorPath = d.getRelativePathToHost();
        return !vendorPath.equals("vendor/" + name);
    }

    private boolean vendorDirShouldBeReserved(File dir, File vendorDir, GolangDependencySet existedVendorDependencies) {
        String packagePath = StringUtils.toUnixString(vendorDir.toPath().relativize(dir.toPath()));
        return existedVendorDependencies.findByName(packagePath).isPresent();
    }

    private boolean isGogradleVendorDependency(GolangDependency dependency) {
        if (dependency instanceof VendorResolvedDependency) {
            VendorResolvedDependency vendorResolvedDependency = (VendorResolvedDependency) dependency;
            return vendorResolvedDependency.getHostDependency() instanceof GogradleRootProject;
        } else {
            return false;
        }
    }

}
