package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.tasks.TaskAction;

import static com.github.blindpirate.gogradle.core.GolangTaskContainer.RESOLVE_TASK_NAME;

/**
 * Prints the dependencies tree.
 */
public class DependenciesTask extends AbstractGolangTask {
    public DependenciesTask() {
        dependsOn(RESOLVE_TASK_NAME);
    }

    @TaskAction
    public void displayDependencies() {
        ResolveTask task = getTask(ResolveTask.class);
        DependencyTreeNode tree = task.getDependencyTree();
        display(tree);
    }

    private void display(DependencyTreeNode tree) {
        System.out.println(tree.output());
    }
}
