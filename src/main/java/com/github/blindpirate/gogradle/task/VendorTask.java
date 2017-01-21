package com.github.blindpirate.gogradle.task;

import org.gradle.api.tasks.TaskAction;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;

public class VendorTask extends AbstractGolangTask {

    public VendorTask() {
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    void vendor() {

    }
}
