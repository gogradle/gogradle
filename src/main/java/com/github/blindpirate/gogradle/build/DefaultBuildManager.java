package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.exceptions.BuildException;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.MapUtils.asMap;
import static java.util.Arrays.asList;

// ${projectRoot}
// └── .gogradle
//     ├── project_gopath
//     │   └── src
//     │       └── github.com/user/project -> ../../../../../..
//     ├── build_gopath
//     │   └── src
//     │       └── <the dependencies>
//     ├── test_gopath
//     │   └── src
//     │       └── <the dependencies>
//     └── ${os}_${arch}_${outputName}
//

@Singleton
public class DefaultBuildManager implements BuildManager {
    private static final String GOGRADLE_BUILD_DIR = ".gogradle";
    private static final String BUILD_GOPATH = "build_gopath";
    private static final String TEST_GOPATH = "test_gopath";
    private static final String PROJECT_GOPATH = "project_gopath";
    private static final String SRC = "src";

    private static final Logger LOGGER = Logging.getLogger(DefaultBuildManager.class);

    private static final String OUTPUT_FILE_NAME = "%s_%s_%s";

    private final Project project;
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;

    @Override
    public void prepareForBuild() {
        createProjectSymbolicLinkIfNotExist();
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void createProjectSymbolicLinkIfNotExist() {
        Path link = getGogradleBuildDir()
                .resolve(PROJECT_GOPATH)
                .resolve(SRC)
                .resolve(setting.getPackagePath());
        if (!link.toFile().exists()) {
            forceMkdir(link.getParent().toFile());
            Path targetPath = project.getRootDir().toPath();
            createSymbolicLink(link, link.getParent().relativize(targetPath));
        }
    }

    private void createSymbolicLink(Path link, Path target) {
        try {
            Files.createSymbolicLink(link, target);
        } catch (IOException e) {
            throw BuildException.cannotCreateSymbolicLink(link, e);
        }
    }

    private Path getTestGopathDir() {
        return getGogradleBuildDir().resolve(TEST_GOPATH);
    }

    private Path getBuildGopathDir() {
        return getGogradleBuildDir().resolve(BUILD_GOPATH);
    }

    private Path getGogradleBuildDir() {
        return project.getRootDir().toPath()
                .resolve(GOGRADLE_BUILD_DIR);
    }

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
        String gopathForBuild = getGopathForBuild();

        List<String> args = asList(goBinary, "build", "-o", outputFilePath);

        Map<String, String> envs = asMap("GOPATH", gopathForBuild);
        if (gorootEnv != null) {
            envs.put("GOROOT", gorootEnv);
        }

        startBuild(args, envs);
    }

    private String getGopathForBuild() {
        String projectGopath = getGogradleBuildDir()
                .resolve(PROJECT_GOPATH)
                .toAbsolutePath()
                .toString();
        String buildGopath = getGogradleBuildDir()
                .resolve(BUILD_GOPATH)
                .toAbsolutePath()
                .toString();

        return projectGopath + File.pathSeparator + buildGopath;
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
        File targetDir = getBuildGopathDir()
                .resolve(SRC)
                .resolve(dependency.getName())
                .toFile();

        forceMkdir(targetDir);
        clearDirectory(targetDir);
        dependency.installTo(targetDir);
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
