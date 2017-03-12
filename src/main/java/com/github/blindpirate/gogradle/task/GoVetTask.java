package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import org.gradle.api.Task;

import javax.inject.Inject;
import java.util.Arrays;

public class GoVetTask extends Go {
    @Inject
    private GolangPluginSetting setting;

    public GoVetTask() {
        dependsOn(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(this::execute);
    }

    private void execute(Task task) {
        buildManager.go(Arrays.asList("tool", "vet", setting.getPackagePath()), null);
    }
}
