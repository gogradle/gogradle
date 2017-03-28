package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;
import com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public class GofmtTask extends Go {
    @Inject
    private GoBinaryManager goBinaryManager;

    public GofmtTask() {
        dependsOn(GolangTaskContainer.PREPARE_TASK_NAME);
    }

    protected void doAddDefaultAction() {
        doLast(task -> run(CollectionUtils.asStringList(getGofmtPath(), "-w", getAllFilesToFormat())));
    }

    // A workaround because gofmt on Windows doesn't ignore .gogradle
    private List<String> getAllFilesToFormat() {
        return IOUtils.safeListFiles(getProject().getRootDir())
                .stream()
                .filter(file -> !file.getName().startsWith("."))
                .filter(file -> !VendorDependencyFactory.VENDOR_DIRECTORY.equals(file.getName()))
                .filter(file -> file.isDirectory() || file.getName().endsWith(".go"))
                .map(file -> toUnixString(file.getAbsolutePath()))
                .collect(Collectors.toList());
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
