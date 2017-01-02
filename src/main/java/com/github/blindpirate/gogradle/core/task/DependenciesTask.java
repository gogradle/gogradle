package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

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
        ResolveTask task = (ResolveTask) getProject().getTasksByName(RESOLVE_TASK_NAME, false)
                .stream().findFirst().get();
        DependencyTreeNode tree = task.getDependencyTree();
        display(tree);
    }

    private void display(DependencyTreeNode tree) {
        System.out.println(tree.output());
    }
}
