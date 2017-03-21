package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;


public class InstallBuildDependenciesTask extends AbstractGolangTask {
    @Inject
    private BuildManager buildManager;

    public InstallBuildDependenciesTask() {
        dependsOn(GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    public void installDependencies() {
        DependencyTreeNode rootNode = getTask(ResolveBuildDependenciesTask.class).getDependencyTree();
        rootNode.flatten()
                .stream()
                .map(dependency -> (ResolvedDependency) dependency)
                .forEach((dependency) -> buildManager.installDependency(dependency, BUILD));
    }
}
