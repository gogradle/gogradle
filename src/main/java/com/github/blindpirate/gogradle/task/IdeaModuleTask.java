package com.github.blindpirate.gogradle.task;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME;

public class IdeaModuleTask extends AbstractGolangTask {
    @Inject
    private Project project;

    public IdeaModuleTask() {
        dependsOn(INSTALL_BUILD_DEPENDENCIES_TASK_NAME, INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    public void ideaModule() {

    }
}
