package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;

import javax.inject.Inject;
import java.nio.file.Path;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public class GofmtTask extends Go {
    @Inject
    private GoBinaryManager goBinaryManager;

    public GofmtTask() {
        dependsOn(GolangTaskContainer.PREPARE_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(task
                -> run(getGofmtPath() + " -w " + toUnixString(getProject().getRootDir().getAbsolutePath())));
    }

    private String getGofmtPath() {
        Path goBinPath = goBinaryManager.getBinaryPath();
        Path gofmtPath = goBinPath.resolve("../gofmt").normalize();
        return toUnixString(gofmtPath.toAbsolutePath());
    }

    public void gofmt(String arg) {
        run(getGofmtPath() + " " + arg);
    }
}
