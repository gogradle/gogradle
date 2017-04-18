package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;

public abstract class InstallTask extends AbstractGolangTask {

    @Inject
    private BuildManager buildManager;

    @TaskAction
    public void installDependencies() {
        clearDirectory(buildManager.getInstallationDirectory(getConfigurationName()).toFile());
        DependencyTreeNode dependencyTree = getUpstreamResolveTask().getDependencyTree();
        dependencyTree.flatten()
                .stream()
                .map(dependency -> (ResolvedDependency) dependency)
                .forEach((dependency) -> buildManager.installDependency(dependency, getConfigurationName()));
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
