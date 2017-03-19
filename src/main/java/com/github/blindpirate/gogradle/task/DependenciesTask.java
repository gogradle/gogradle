package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME;

/**
 * Prints the dependencies tree.
 */
public class DependenciesTask extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(DependenciesTask.class);

    public DependenciesTask() {
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME,
                RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    public void displayDependencies() {
        DependencyTreeNode buildTree = getTask(ResolveBuildDependenciesTask.class).getDependencyTree();
        DependencyTreeNode testTree = getTask(ResolveTestDependenciesTask.class).getDependencyTree();
        display(BUILD, buildTree);
        display(TEST, testTree);
    }

    private void display(String configuration, DependencyTreeNode tree) {
        LOGGER.quiet(configuration + ":");
        LOGGER.quiet(tree.output());
    }
}
