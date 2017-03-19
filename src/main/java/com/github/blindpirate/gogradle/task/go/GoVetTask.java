package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.task.GolangTaskContainer;

public class GoVetTask extends Go {
    private boolean continueWhenFail;

    public void setContinueWhenFail(boolean continueWhenFail) {
        this.continueWhenFail = continueWhenFail;
    }

    public GoVetTask() {
        dependsOn(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(task -> {
            if (continueWhenFail) {
                setRetcodeConsumer(code -> {
                });
            }
            go("vet ./...");
        });
    }
}
