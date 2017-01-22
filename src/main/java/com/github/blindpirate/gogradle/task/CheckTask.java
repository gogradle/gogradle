package com.github.blindpirate.gogradle.task;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*;

public class CheckTask extends AbstractGolangTask {

    public CheckTask() {
        dependsOn(TEST_TASK_NAME);
    }
}
