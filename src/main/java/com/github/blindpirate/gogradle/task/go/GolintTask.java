package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import org.gradle.api.Task;

import javax.inject.Inject;
import java.util.Arrays;

public class GolintTask extends Go {
    @Inject
    private GolangPluginSetting setting;

    public GolintTask() {
        dependsOn(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(this::execute);
    }

    private void execute(Task task) {
        buildManager.run(Arrays.asList("golint", setting.getPackagePath()), null);
    }
}
