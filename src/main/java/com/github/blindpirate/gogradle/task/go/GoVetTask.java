package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME;

public class GoVetTask extends Go {
    public GoVetTask() {
        dependsOn(INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(task -> go("vet ./..."));
    }
}
