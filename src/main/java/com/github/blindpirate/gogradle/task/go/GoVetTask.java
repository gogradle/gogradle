package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;

public class GoVetTask extends Go {
    public GoVetTask() {
        dependsOn(GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME,
                GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(task -> go("vet ./..."));
    }
}
