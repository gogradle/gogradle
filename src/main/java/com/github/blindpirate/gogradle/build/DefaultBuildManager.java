package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.exceptions.BuildException;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.build.Configuration.BUILD;
import static com.github.blindpirate.gogradle.build.Configuration.TEST;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.CollectionUtils.asStringList;
import static com.github.blindpirate.gogradle.util.IOUtils.clearDirectory;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;

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
    private static final Predicate<String> NO_TEST_FILES_FILTER = line -> !line.contains("[no test files]");

    private final Project project;
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;

    @Override
    public void ensureDotVendorDirNotExist() {
        if (new File(project.getRootDir(), "." + VENDOR_DIRECTORY).exists()) {
            throw new IllegalStateException("We need .vendor directory as temp directory, "
                    + "existent .vendor before build is not allowed.");
        }
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

    private void renameVendorDuringBuild(Runnable runnable) {
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

    @Override
    public void go(List<String> args, Map<String, String> env) {
        go(args, env, null, null, null);
    }

    @Override
    public void run(List<String> args, Map<String, String> env) {
        run(args, env, null, null, null);

    }

    @Override
    public void go(List<String> args,
                   Map<String, String> env,
                   Consumer<String> stdoutLineConsumer,
                   Consumer<String> stderrLineConsumer,
                   Consumer<Integer> retcodeConsumer) {
        List<String> cmdAndArgs = asStringList(getGoBinary(), args);
        run(cmdAndArgs, env, stdoutLineConsumer, stderrLineConsumer, retcodeConsumer);
    }

    @Override
    public void run(List<String> args, Map<String, String> env,
                    Consumer<String> stdoutLineConsumer,
                    Consumer<String> stderrLineConsumer,
                    Consumer<Integer> retcodeConsumer) {
        renameVendorDuringBuild(() -> {
            Map<String, String> finalEnv = determineEnv(env);

            @SuppressWarnings("unchecked")
            List<String> finalArgs = renderArgs(args, (Map) finalEnv);

            doRun(finalArgs, finalEnv, stdoutLineConsumer, stderrLineConsumer, retcodeConsumer);
        });
    }

    @SuppressWarnings("unchecked")
    private List<String> renderArgs(List<String> args, Map<String, String> env) {
        Map<String, Object> context = new HashMap<>(env);
        context.put("PROJECT_NAME", project.getName());
        return args.stream().map(s -> StringUtils.render(s, context)).collect(Collectors.toList());
    }

    private void doRun(List<String> args,
                       Map<String, String> env,
                       Consumer<String> stdoutLineConsumer,
                       Consumer<String> stderrLineConsumer,
                       Consumer<Integer> retcodeConsumer) {
        stdoutLineConsumer = stdoutLineConsumer == null ? LOGGER::quiet : stdoutLineConsumer;
        stderrLineConsumer = stderrLineConsumer == null ? LOGGER::error : stderrLineConsumer;
        retcodeConsumer = retcodeConsumer == null ? this::ensureProcessReturnZero : retcodeConsumer;

        Process process = ProcessUtils.run(args, determineEnv(env), project.getRootDir());

        CountDownLatch latch = new CountDownLatch(2);

        new SubprocessReader(process::getInputStream, stdoutLineConsumer, latch).start();
        new SubprocessReader(process::getErrorStream, stderrLineConsumer, latch).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw ExceptionHandler.uncheckException(e);
        }

        try {
            int retcode = process.waitFor();

            retcodeConsumer.accept(retcode);
        } catch (InterruptedException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private void ensureProcessReturnZero(int retcode) {
        if (retcode != 0) {
            throw BuildException.processReturnNonZero(retcode);
        }
    }

    private Map<String, String> determineEnv(Map<String, String> env) {
        Map<String, String> defaultEnvs = MapUtils.asMap("GOPATH", getTestGopath(),
                "GOROOT", goBinaryManager.getGoroot().toAbsolutePath().toString());
        if (env != null) {
            defaultEnvs.putAll(env);
        }
        return defaultEnvs;
    }

    private String getGoBinary() {
        return goBinaryManager.getBinaryPath().toAbsolutePath().toString();
    }


    public String getBuildGopath() {
        String projectGopath = getGogradleBuildDir()
                .resolve(PROJECT_GOPATH)
                .toAbsolutePath()
                .toString();
        String buildGopath = getGopathDir(BUILD)
                .toAbsolutePath()
                .toString();

        return projectGopath + File.pathSeparator + buildGopath;
    }

    public String getTestGopath() {
        String testGopath = getGopathDir(TEST)
                .toAbsolutePath()
                .toString();

        return getBuildGopath() + File.pathSeparator + testGopath;
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
}
