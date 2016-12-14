package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.GolangPlugin;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyTreeNode;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class DependenciesTask extends DefaultTask {
    @TaskAction
    public void displayDependencies() {
        ResolveTask task = (ResolveTask) getProject().getTasksByName(GolangPlugin.PREPARE_TASK_NAME, false);
        DependencyTreeNode tree = task.getDependencyTree();
        display(tree);
    }

    private void display(DependencyTreeNode tree) {
    }
}
