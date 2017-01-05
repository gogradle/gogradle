package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.dependency.DependencyInstaller;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.core.GolangTaskContainer.RESOLVE_TASK_NAME;

public class InstallDependenciesTask extends AbstractGolangTask {
    @Inject
    private DependencyInstaller dependencyInstaller;

    public InstallDependenciesTask() {
        dependsOn(RESOLVE_TASK_NAME);
    }

    @TaskAction
    void installDependencies() {
        DependencyTreeNode rootNode = getTask(ResolveTask.class).getDependencyTree();
        rootNode.flatten()
                .stream()
                .map(GolangDependency::resolve)
                .forEach(dependencyInstaller::installDependency);
    }
}
