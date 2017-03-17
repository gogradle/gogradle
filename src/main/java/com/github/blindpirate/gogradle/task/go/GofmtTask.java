package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import java.nio.file.Path;

import static java.util.Arrays.asList;

public class GofmtTask extends Go {
    private static final Logger LOGGER = Logging.getLogger(GofmtTask.class);
    @Inject
    private GoBinaryManager goBinaryManager;

    public GofmtTask() {
        dependsOn(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME,
                GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(this::execute);
    }

    private void execute(Task task) {
        buildManager.run(asList(getGofmtPath(), "-w", "-s", "."), null);
    }

    private String getGofmtPath() {
        Path goBinPath = goBinaryManager.getBinaryPath();
        Path gofmtPath = goBinPath.resolve("../gofmt").normalize();
        return gofmtPath.toAbsolutePath().toString();
    }

    public void gofmt(String arg) {
        this.doLast(task ->
                buildManager.run(CollectionUtils.asStringList(getGofmtPath(), extractArgs(arg)), null));
    }
}
