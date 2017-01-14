package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.ensureDirExistAndWritable;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.MapUtils.asMap;
import static java.util.Arrays.asList;

@Singleton
public class DefaultBuildManager implements BuildManager {
    private static final Logger LOGGER = Logging.getLogger(DefaultBuildManager.class);

    private static final String OUTPUT_FILE_NAME = "%s_%s_%s";

    private final Project project;
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;

    @Inject
    public DefaultBuildManager(Project project,
                               GoBinaryManager goBinaryManager,
                               GolangPluginSetting setting) {
        this.project = project;
        this.goBinaryManager = goBinaryManager;
        this.setting = setting;
    }

    @Override
    public void build() {
        String goBinary = goBinaryManager.getBinaryPath();
        String gorootEnv = goBinaryManager.getGorootEnv();
        String outputFilePath = getOutputFilePath();
        String projectGopath = ensureProjectGopathWritable().toString();

        List<String> args = asList(goBinary, "-o", outputFilePath);

        Map<String, String> envs = asMap("GOPATH", projectGopath);
        if (gorootEnv != null) {
            envs.put("GOROOT", gorootEnv);
        }

        startBuild(args, envs);
    }

    private void startBuild(List<String> args, Map<String, String> envs) {
        Process process = ProcessUtils.run(args, envs, project.getRootDir());

        CountDownLatch latch = new CountDownLatch(2);

        new SubprocessReader(process::getInputStream, LOGGER::quiet, latch).start();
        new SubprocessReader(process::getErrorStream, LOGGER::error, latch).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    @Override
    public void installDependency(ResolvedDependency dependency) {
        File targetDir = ensureProjectGopathWritable().resolve(dependency.getName()).toFile();
        forceMkdir(targetDir);
        clearDirectory(targetDir);
        dependency.installTo(targetDir);
    }

    private Path ensureProjectGopathWritable() {
        Path ret = project.getRootDir().toPath()
                .resolve(GOGRADLE_BUILD_DIR)
                .resolve(BUILD_GOPATH);
        return ensureDirExistAndWritable(ret);
    }

    private String getOutputFilePath() {
        String packageName = Objects.toString(Paths.get(setting.getPackagePath()).getFileName());
        String outputFileName = String.format(OUTPUT_FILE_NAME,
                Os.getHostOs(),
                Arch.getHostArch(),
                packageName);
        return project.getRootDir().toPath()
                .resolve(GOGRADLE_BUILD_DIR)
                .resolve(outputFileName)
                .toString();
    }
}
