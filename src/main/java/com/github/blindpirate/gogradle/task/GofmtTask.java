package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import org.gradle.api.Task;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Arrays;

public class GofmtTask extends Go {
    @Inject
    private GoBinaryManager goBinaryManager;
    @Inject
    private GolangPluginSetting setting;

    public GofmtTask() {
        dependsOn(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(this::execute);
    }

    private void execute(Task task) {
        Path goBinPath = goBinaryManager.getBinaryPath();
        Path gofmtPath = goBinPath.resolve("../gofmt").normalize();

        buildManager.run(Arrays.asList(gofmtPath.toAbsolutePath().toString(), setting.getPackagePath()), null);
    }
}
