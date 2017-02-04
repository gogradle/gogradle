package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.exceptions.BuildException;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.build.Configuration.BUILD;
import static com.github.blindpirate.gogradle.build.Configuration.TEST;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.filterTestsMatchingPatterns;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.MapUtils.asMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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
    private static final String GOPATH = "%s_gopath";
    private static final String PROJECT_GOPATH = "project_gopath";
    private static final String SRC = "src";

    private static final Logger LOGGER = Logging.getLogger(DefaultBuildManager.class);
    // https://golang.org/cmd/go/#hdr-Description_of_package_lists
    private static final String ALL_TEST = "./...";
    private static final Predicate<String> NO_TEST_FILES_FILTER = line -> !line.contains("[no test files]");

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
                .resolve(GogradleGlobal.GOGRADLE_BUILD_DIR_NAME);
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
            if (vendorDir.exists()) {
                if (targetDir.exists() || !vendorDir.renameTo(targetDir)) {
                    throw BuildException.cannotRenameVendorDir(targetDir);
                }
            }
            runnable.run();
        } finally {
            if (targetDir.exists()) {
                if (vendorDir.exists() || !targetDir.renameTo(vendorDir)) {
                    throw BuildException.cannotRenameVendorDir(vendorDir);
                }
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

        int retCode = startBuildOrTest(args, envs);
        if (retCode != 0) {
            throw BuildException.processReturnNonZero(retCode);
        }
    }

    @Override
    public void test() {
        testWithTargets(Arrays.asList(ALL_TEST));
    }

    private void testWithTargets(List<String> targets) {
        autoRecoverVendor(() -> {
            String goBinary = goBinaryManager.getBinaryPath();
            String gopath = getTestGopath();

            Map<String, String> envs = getEnvs(gopath);
            List<String> args = Lists.newArrayList(goBinary, "test");
            args.addAll(targets);
            args.addAll(setting.getExtraTestArgs());

            int retCode = startBuildOrTest(args, envs);
            if (retCode != 0) {
                throw BuildException.processReturnNonZero(retCode);
            }
        });
    }

    @Override
    public void testWithPatterns(List<String> testNamePattern) {
        Collection<File> filesMatchingPatterns = filterTestsMatchingPatterns(project.getRootDir(), testNamePattern);
        if (filesMatchingPatterns.isEmpty()) {
            LOGGER.quiet("No tests matching " + testNamePattern.stream().collect(joining("/")) + ", skip.");
        } else {
            LOGGER.quiet("Found " + filesMatchingPatterns.size() + " tests to run.");

            Map<File, List<File>> groupByParentDir = filesMatchingPatterns.stream()
                    .collect(Collectors.groupingBy(File::getParentFile));

            groupByParentDir.forEach((parentDir, tests) -> {
                List<String> fullPaths = tests.stream()
                        .map(File::getAbsolutePath).collect(toList());
                fullPaths.addAll(getAllNonTestGoFiles(parentDir));
                testWithTargets(fullPaths);
            });
        }
    }

    private List<String> getAllNonTestGoFiles(File dir) {
        List<String> names = IOUtils.safeList(dir);
        return names.stream()
                .filter(name -> name.endsWith(".go"))
                .filter(name -> !StringUtils.startsWithAny(name, "_", "."))
                .filter(name -> !name.endsWith("_test.go"))
                .map(name -> new File(dir, name))
                .map(File::getAbsolutePath)
                .collect(toList());
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

    private int startBuildOrTest(List<String> args, Map<String, String> envs) {
        Process process = ProcessUtils.run(args, envs, project.getRootDir());

        CountDownLatch latch = new CountDownLatch(2);

        new SubprocessReader(process::getInputStream, LOGGER::quiet, latch, NO_TEST_FILES_FILTER).start();
        new SubprocessReader(process::getErrorStream, LOGGER::error, latch).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw ExceptionHandler.uncheckException(e);
        }

        try {
            return process.waitFor();
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
        installTo(dependency, targetDir);
    }

    private void installTo(ResolvedDependency dependency, File targetDir) {
        forceMkdir(targetDir);
        clearDirectory(targetDir);
        dependency.installTo(targetDir);
    }

    @Override
    public void installDependencyToVendor(ResolvedDependency dependency) {
        File targetDir = project.getRootDir().toPath()
                .resolve(VENDOR_DIRECTORY)
                .resolve(dependency.getName())
                .toFile();
        installTo(dependency, targetDir);
    }

    private List<String> determineOutputFilePaths() {
        return setting.getTargetPlatforms().stream()
                .map(this::determineOne)
                .collect(toList());
    }

    private String determineOne(Pair<Os, Arch> osAndArch) {
        String packageName = Objects.toString(Paths.get(setting.getPackagePath()).getFileName());
        String outputFileName = setting.getOutputPattern();
        outputFileName = outputFileName.replaceAll("\\$\\{os}", osAndArch.getLeft().toString());
        outputFileName = outputFileName.replaceAll("\\$\\{arch}", osAndArch.getRight().toString());
        outputFileName = outputFileName.replaceAll("\\$\\{packageName}", packageName);
        outputFileName = outputFileName.replaceAll("\\$\\{extension}", osAndArch.getLeft().exeExtension());

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
