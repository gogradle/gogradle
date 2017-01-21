package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.exceptions.BuildException;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.build.Configuration.BUILD;
import static com.github.blindpirate.gogradle.build.Configuration.TEST;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.IOUtils.isValidDirectory;
import static com.github.blindpirate.gogradle.util.MapUtils.asMap;

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
    private static final String GOPATH = "%s_gopath";
    private static final String PROJECT_GOPATH = "project_gopath";
    private static final String SRC = "src";

    private static final Logger LOGGER = Logging.getLogger(DefaultBuildManager.class);

    private final Project project;
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;

    @Override
    public void ensureDotVendorDirNotExist() {
        Assert.isTrue(!new File(project.getRootDir(), "." + VENDOR_DIRECTORY).exists(),
                "We need .vendor directory as temp directory, existent .vendor before build is not allowed.");
    }

    @Override
    public void prepareSymbolicLinks() {
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

    private Path getGopathDir(Configuration configuration) {
        String gopathDirName = String.format(GOPATH, configuration.getName());
        return getGogradleBuildDir().resolve(gopathDirName);
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
        autoRecoverVendor(() -> {
            String goBinary = goBinaryManager.getBinaryPath();
            String gopath = getBuildGopath();
            Map<String, String> envs = getEnvs(gopath);
            determineOutputFilePaths().forEach(outputFilePath ->
                    buildOne(goBinary, outputFilePath, envs)
            );
        });
    }

    private void autoRecoverVendor(Runnable runnable) {
        File vendorDir = new File(project.getRootDir(), VENDOR_DIRECTORY);
        File targetDir = new File(project.getRootDir(), "." + VENDOR_DIRECTORY);
        try {
            if (isValidDirectory(vendorDir) && !vendorDir.renameTo(targetDir)) {
                throw BuildException.cannotRenameVendorDir(targetDir);
            }
            runnable.run();
        } finally {
            if (isValidDirectory(targetDir) && !targetDir.renameTo(vendorDir)) {
                throw BuildException.cannotRenameVendorDir(vendorDir);
            }
        }
    }

    private Map<String, String> getEnvs(String gopath) {
        String gorootEnv = goBinaryManager.getGorootEnv();
        Map<String, String> envs = asMap("GOPATH", gopath);
        if (gorootEnv != null) {
            envs.put("GOROOT", gorootEnv);
        }

        return envs;
    }

    private void buildOne(String goBinary, String outputFilePath, Map<String, String> envs) {
        List<String> args = Lists.newArrayList(goBinary, "build", "-o", outputFilePath);
        args.addAll(setting.getExtraBuildArgs());

        startBuildOrTest(args, envs);
    }

    @Override
    public void test() {
        autoRecoverVendor(() -> {
            String goBinary = goBinaryManager.getBinaryPath();
            String gopath = getTestGopath();

            Map<String, String> envs = getEnvs(gopath);
            List<String> args = Lists.newArrayList(goBinary, "test");
            args.addAll(setting.getExtraTestArgs());

            startBuildOrTest(args, envs);
        });
    }

    private String getBuildGopath() {
        String projectGopath = getGogradleBuildDir()
                .resolve(PROJECT_GOPATH)
                .toAbsolutePath()
                .toString();
        String buildGopath = getGopathDir(BUILD)
                .toAbsolutePath()
                .toString();

        return projectGopath + File.pathSeparator + buildGopath;
    }

    private String getTestGopath() {
        String testGopath = getGopathDir(TEST)
                .toAbsolutePath()
                .toString();

        return getBuildGopath() + File.pathSeparator + testGopath;
    }

    private void startBuildOrTest(List<String> args, Map<String, String> envs) {
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
    public void installDependency(ResolvedDependency dependency, Configuration configuration) {
        File targetDir = getGopathDir(configuration)
                .resolve(SRC)
                .resolve(dependency.getName())
                .toFile();

        forceMkdir(targetDir);
        clearDirectory(targetDir);
        dependency.installTo(targetDir);
    }

    private List<String> determineOutputFilePaths() {
        return setting.getTargetPlatforms().stream()
                .map(this::determineOne)
                .collect(Collectors.toList());
    }

    private String determineOne(Pair<Os, Arch> osAndArch) {
        String packageName = Objects.toString(Paths.get(setting.getPackagePath()).getFileName());
        String outputFileName = setting.getOutputPattern();
        outputFileName = outputFileName.replaceAll("\\$\\{os}", osAndArch.getLeft().toString());
        outputFileName = outputFileName.replaceAll("\\$\\{arch}", osAndArch.getRight().toString());
        outputFileName = outputFileName.replaceAll("\\$\\{packageName}", packageName);

        Path outputLocationPath = Paths.get(setting.getOutputLocation());

        if (outputLocationPath.isAbsolute()) {
            return outputLocationPath.resolve(outputFileName).toString();
        } else {
            return project.getRootDir().toPath()
                    .resolve(outputLocationPath)
                    .resolve(outputFileName)
                    .normalize()
                    .toString();
        }
    }
}
