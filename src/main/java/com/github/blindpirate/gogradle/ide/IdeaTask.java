package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RENAME_VENDOR_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME;

public class IdeaTask extends AbstractGolangTask {

    @Inject
    private IdeaIntegration ideaIntegration;

    public IdeaTask() {
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME,
                RESOLVE_TEST_DEPENDENCIES_TASK_NAME,
                RENAME_VENDOR_TASK_NAME);
    }

    @TaskAction
    public void generateXmlsForIdea() {
        ideaIntegration.generateXmls();
    }
}
