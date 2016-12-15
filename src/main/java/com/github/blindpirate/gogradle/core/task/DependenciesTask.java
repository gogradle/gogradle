package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.core.dependency.produce.DependencyTreeNode;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import static com.github.blindpirate.gogradle.GolangPlugin.PREPARE_TASK_NAME;
import static com.github.blindpirate.gogradle.GolangPlugin.RESOLVE_TASK_NAME;

/**
 * Prints the dependencies tree.
 */
public class DependenciesTask extends DefaultTask {

    public DependenciesTask() {
        dependsOn(RESOLVE_TASK_NAME);
    }

    @TaskAction
    public void displayDependencies() {
        ResolveTask task = (ResolveTask) getProject().getTasksByName(PREPARE_TASK_NAME, false);
        DependencyTreeNode tree = task.getDependencyTree();
        display(tree);
    }

    private void display(DependencyTreeNode tree) {
    }
}
