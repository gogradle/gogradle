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

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

public abstract class InstallTask extends AbstractGolangTask {
    private static final String CURRENT_VERSION_META = ".CURRENT_VERSION";
    private static final Logger LOGGER = Logging.getLogger(InstallTask.class);

    @Inject
    private BuildManager buildManager;

    @TaskAction
    public void installDependencies() {
        File src = buildManager.getInstallationDirectory(getConfigurationName()).resolve("src").toFile();
        IOUtils.forceMkdir(src);

        DependencyTreeNode dependencyTree = getUpstreamResolveTask().getDependencyTree();
        GolangDependencySet flatDependencies = dependencyTree.flatten();

        IOUtils.markAndDelete(src, dir -> currentVersionMatchDependency(dir, src, flatDependencies));

        flatDependencies.forEach(this::installIfNecessary);
    }

    private void installIfNecessary(GolangDependency dependency) {
        File targetDir = buildManager.getInstallationDirectory(getConfigurationName())
                .resolve("src")
                .resolve(dependency.getName())
                .toFile();
        IOUtils.forceMkdir(targetDir);
        if (IOUtils.dirIsEmpty(targetDir)) {
            IOUtils.write(targetDir, CURRENT_VERSION_META, dependency.getVersion());
            ResolvedDependency.class.cast(dependency).installTo(targetDir);
        } else {
            LOGGER.debug("{} is not empty, skip installing {}", targetDir, dependency);
        }
    }


    private boolean currentVersionMatchDependency(File currentDir,
                                                  File srcDir,
                                                  GolangDependencySet dependencySet) {
        String packagePath = StringUtils.toUnixString(srcDir.toPath().relativize(currentDir.toPath()));
        Optional<GolangDependency> existed = dependencySet.findByName(packagePath);
        if (!existed.isPresent()) {
            return false;
        }

        File versionFile = new File(currentDir, CURRENT_VERSION_META);
        if (!versionFile.exists()) {
            return false;
        }

        ResolvedDependency resolvedDependency = (ResolvedDependency) (existed.get());
        if (isOnDisk(resolvedDependency)) {
            // For the sake of security, we would always think local dependency as out-of-date
            return false;
        } else {
            return IOUtils.toString(new File(currentDir, CURRENT_VERSION_META))
                    .equals(resolvedDependency.getVersion());
        }
    }

    private boolean isOnDisk(ResolvedDependency dependency) {
        if (dependency instanceof LocalDirectoryDependency) {
            return true;
        } else if (dependency instanceof VendorResolvedDependency) {
            VendorResolvedDependency vendorResolvedDependency = (VendorResolvedDependency) dependency;
            return vendorResolvedDependency.getHostDependency() instanceof LocalDirectoryDependency;
        } else {
            return false;
        }
    }

    @InputFile
    public File getSerializationFile() {
        return getUpstreamResolveTask().getSerializationFile();
    }

    @OutputDirectory
    public File getInstallationDirectory() {
        return buildManager.getInstallationDirectory(getConfigurationName()).toFile();
    }

    private String getConfigurationName() {
        return getUpstreamResolveTask().getConfigurationName();
    }

    protected abstract ResolveTask getUpstreamResolveTask();
}
