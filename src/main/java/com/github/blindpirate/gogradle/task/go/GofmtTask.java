package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.StringUtils;

import javax.inject.Inject;
import java.nio.file.Path;

public class GofmtTask extends Go {
    @Inject
    private GoBinaryManager goBinaryManager;

    public GofmtTask() {
        dependsOn(GolangTaskContainer.PREPARE_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(task -> {
            super.run(getGofmtPath() + " -w .");
        });
    }

    private String getGofmtPath() {
        Path goBinPath = goBinaryManager.getBinaryPath();
        Path gofmtPath = goBinPath.resolve("../gofmt").normalize();
        return StringUtils.toUnixString(gofmtPath.toAbsolutePath());
    }

    public void gofmt(String arg) {
        super.run(getGofmtPath() + " " + arg);
    }
}
