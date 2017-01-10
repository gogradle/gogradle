package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TASK_NAME;

// TODO old dependencies should be deleted before installation
public class InstallDependenciesTask extends AbstractGolangTask {
    @Inject
    private BuildManager buildManager;

    public InstallDependenciesTask() {
        dependsOn(RESOLVE_TASK_NAME);
    }

    @TaskAction
    public void installDependencies() {
        DependencyTreeNode rootNode = getTask(ResolveTask.class).getDependencyTree();
        rootNode.flatten()
                .stream()
                .map(GolangDependency::resolve)
                .forEach(buildManager::installDependency);
    }
}
