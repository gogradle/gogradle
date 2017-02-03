package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;

public class VendorTask extends AbstractGolangTask {

    @Inject
    private Project project;
    @Inject
    private BuildManager buildManager;

    public VendorTask() {
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    void vendor() {
        File vendorDir = new File(project.getRootDir(), VendorDependencyFactory.VENDOR_DIRECTORY);
        if (vendorDir.exists()) {
            Assert.isTrue(vendorDir.isDirectory(), "Vendor must be a directory!");
            clearAllSubDirectories(vendorDir);
        }

        DependencyTreeNode buildTree = getTask(ResolveBuildDependenciesTask.class).getDependencyTree();

        buildTree.flatten().stream()
                .map(dependency -> (ResolvedDependency) dependency)
                .forEach(buildManager::installDependencyToVendor);
    }

    private void clearAllSubDirectories(File vendorDir) {
        List<String> names = IOUtils.safeList(vendorDir);
        names.stream()
                .map(name -> new File(vendorDir, name))
                .filter(File::isDirectory)
                .forEach(IOUtils::forceDelete);
    }
}
