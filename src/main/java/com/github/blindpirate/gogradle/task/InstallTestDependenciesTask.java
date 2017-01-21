package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.build.Configuration.TEST;


public class InstallTestDependenciesTask extends AbstractGolangTask {
    @Inject
    private BuildManager buildManager;

    public InstallTestDependenciesTask() {
        dependsOn(GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    public void installDependencies() {
        DependencyTreeNode rootNode = getTask(ResolveTestDependenciesTask.class).getDependencyTree();
        rootNode.flatten()
                .stream()
                .map(GolangDependency::resolve)
                .forEach((dependency) -> buildManager.installDependency(dependency, TEST));
    }
}
