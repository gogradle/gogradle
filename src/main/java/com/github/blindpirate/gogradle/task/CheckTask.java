package com.github.blindpirate.gogradle.task;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TEST_TASK_NAME;

public class CheckTask extends AbstractGolangTask {

    public CheckTask() {
        dependsOn(TEST_TASK_NAME);
    }
}
