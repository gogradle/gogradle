package com.github.blindpirate.gogradle.task;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.COVERAGE_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.GOFMT_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.GOVET_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TEST_TASK_NAME;

public class CheckTask extends AbstractGolangTask {

    public CheckTask() {
        dependsOn(TEST_TASK_NAME, GOFMT_TASK_NAME, GOVET_TASK_NAME, COVERAGE_TASK_NAME);
    }
}
