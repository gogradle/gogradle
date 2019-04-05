/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.exceptions.BuildException;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.util.CollectionUtils.asStringList;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.StringUtils.formatEnv;
import static com.github.blindpirate.gogradle.util.StringUtils.render;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

// ${projectRoot}
// \- .gogradle
//     \-- project_gopath
//     |   \-- src
//     |       \-- github.com/user/project -> ../../../../../..
//     |
//     \-- ${os}_${arch}_${outputName}
//

@Singleton
public class DefaultBuildManager implements BuildManager {
    private static final String SRC = "src";
    private static final List<String> PROJECT_LAYOUT_DIRS = Arrays.asList("cmd", "internal", "pkg");
    private static final Set<String> TOOL_COMMANDS_SUPPORTING_BUILD_TAG = ImmutableSet.of("vet");

    private static final Logger LOGGER = Logging.getLogger(DefaultBuildManager.class);

    private final Project project;
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;
    private final ProcessUtils processUtils;
    private String gopath;
    private List<Path> gopaths = new ArrayList<>();

    @Inject
    public DefaultBuildManager(Project project,
                               GoBinaryManager goBinaryManager,
                               GolangPluginSetting setting,
                               ProcessUtils processUtils) {
        this.project = project;
        this.goBinaryManager = goBinaryManager;
        this.setting = setting;
        this.processUtils = processUtils;
    }

    @Override
    public void prepareProjectGopathIfNecessary() {
        String systemGopath = System.getenv("GOPATH");
        if (currentProjectMatchesGopath(systemGopath)) {
            LOGGER.quiet("Found global GOPATH: {}.", systemGopath);
            gopaths.addAll(Arrays.stream(systemGopath.split(File.pathSeparator))
                    .map(Paths::get).collect(Collectors.toList()));
        }
        gopaths.add(getProjectDir());
        gopaths.add(getVendorDir());
        gopath = joinGopaths();
        LOGGER.quiet("Use project GOPATH: {}", gopath);
    }

    private boolean currentProjectMatchesGopath(String systemGopath) {
        if (StringUtils.isBlank(systemGopath)) {
            return false;
        }
        return Stream.of(systemGopath.split(File.pathSeparator))
                .anyMatch(this::currentProjectMatchesSingleGopath);
    }

    private boolean currentProjectMatchesSingleGopath(String gopath) {
        return Paths.get(gopath)
                .resolve("src")
                .resolve(setting.getPackagePath()).equals(project.getProjectDir().toPath());
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")

    public void createProjectLayoutIfNotExist() {

        if (!getProjectDir().resolve(SRC).toFile().exists()) {
            forceMkdir(getProjectDir().resolve(SRC).toFile());
        }
        List<String> finalProjectDirs = new ArrayList<>();
        finalProjectDirs.addAll(PROJECT_LAYOUT_DIRS);
        if (setting.getApplicationName() != null) {
            finalProjectDirs.add("cmd/" + setting.getApplicationName());
        }
        for (String dir : finalProjectDirs) {
            Path path = getProjectDir()
                    .resolve(SRC)
                    .resolve(setting.getPackagePath())
                    .resolve(dir);

            if (!path.toFile().exists()) {
                forceMkdir(path.toFile());
            }
        }
    }

    private Path getProjectDir() {
        return project.getProjectDir().toPath().toAbsolutePath();
    }

    private Path getVendorDir() {
        return Paths.get(setting.getVendorTargetDir()).toAbsolutePath();
    }

    @Override
    public int go(List<String> args, Map<String, String> env) {
        return go(args, env, null, null);
    }

    private List<String> insertBuildTags(List<String> cmd) {
        if (setting.getBuildTags().isEmpty() || cmd.isEmpty()) {
            return cmd;
        }

        if (isToolCmdButNotSupportTags(cmd)) {
            return cmd;
        }

        // support: go tool vet -tags whatever package
        int tagsOffset = "tool".equals(cmd.get(0)) ? 2 : 1;
        List<String> ret = new ArrayList<>(cmd);
        // https://golang.org/cmd/go/#hdr-Compile_packages_and_dependencies
        ret.add(tagsOffset, "-tags");

        String tagsArg = String.join(" ", setting.getBuildTags());
        ret.add(tagsOffset + 1, "'" + tagsArg + "'");
        return ret;
    }

    // Currently, only go tool vet supports tags:
    // tool vet xxx -> false
    // tool cover xxx -> true
    private boolean isToolCmdButNotSupportTags(List<String> cmd) {
        return cmd.size() >= 2
                && "tool".equals(cmd.get(0))
                && !TOOL_COMMANDS_SUPPORTING_BUILD_TAG.contains(cmd.get(1));
    }

    @Override
    public int go(List<String> args,
                  Map<String, String> env,
                  Consumer<String> stdoutLineConsumer,
                  Consumer<String> stderrLineConsumer) {
        return go(args, env, stdoutLineConsumer, stderrLineConsumer, false);
    }

    @Override
    public int go(List<String> args,
                  Map<String, String> env,
                  Consumer<String> stdoutLineConsumer,
                  Consumer<String> stderrLineConsumer,
                  boolean continueOnFailure) {
        List<String> cmdAndArgs = asStringList(getGoBinary(), insertBuildTags(args));
        return run(cmdAndArgs, env, stdoutLineConsumer, stderrLineConsumer, continueOnFailure);
    }

    @Override
    public int run(List<String> args, Map<String, String> env,
                   Consumer<String> stdoutLineConsumer,
                   Consumer<String> stderrLineConsumer) {
        return run(args, env, stdoutLineConsumer, stderrLineConsumer, false);
    }

    @Override
    public int run(List<String> args, Map<String, String> env,
                   Consumer<String> stdoutLineConsumer,
                   Consumer<String> stderrLineConsumer,
                   boolean continueOnFailure) {
        Map<String, String> finalEnv = determineEnv(env);

        @SuppressWarnings("unchecked")
        List<String> finalArgs = renderArgs(args, (Map) finalEnv);

        return doRun(finalArgs, finalEnv, stdoutLineConsumer, stderrLineConsumer, continueOnFailure);
    }

    @SuppressWarnings("unchecked")
    private List<String> renderArgs(List<String> args, Map<String, String> env) {
        Map<String, Object> context = new HashMap<>(env);
        context.put("PROJECT_NAME", project.getName());
        context.put("PROJECT_VERSION", project.getVersion());
        return args.stream().map(s -> render(s, context)).collect(Collectors.toList());
    }

    private int doRun(List<String> args,
                      Map<String, String> env,
                      Consumer<String> stdoutLineConsumer,
                      Consumer<String> stderrLineConsumer,
                      boolean continueOnFailure) {
        stdoutLineConsumer = stdoutLineConsumer == null ? LOGGER::quiet : stdoutLineConsumer;
        stderrLineConsumer = stderrLineConsumer == null ? LOGGER::error : stderrLineConsumer;

        Process process = processUtils.run(args, determineEnv(env), project.getProjectDir());

        CountDownLatch latch = new CountDownLatch(2);

        new SubprocessReader(process::getInputStream, stdoutLineConsumer, latch).start();
        new SubprocessReader(process::getErrorStream, stderrLineConsumer, latch).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw ExceptionHandler.uncheckException(e);
        }

        try {
            int ret = process.waitFor();
            if (!continueOnFailure) {
                ensureProcessReturnZero(ret, args, env);
            }
            return ret;
        } catch (InterruptedException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private void ensureProcessReturnZero(int retcode, List<String> args, Map<String, String> env) {
        if (retcode != 0) {
            String message = "\nCommand:\n "
                    + String.join(" ", args)
                    + "\nEnv:\n"
                    + formatEnv(env);
            throw BuildException.processInteractionFailed(retcode, message);
        }
    }

    private Map<String, String> determineEnv(Map<String, String> env) {
        Map<String, String> defaultEnvs = MapUtils.asMap("GOPATH", joinGopaths(),
                "GOROOT", toUnixString(goBinaryManager.getGoroot().toAbsolutePath()),
                "GOOS", Os.getHostOs().toString(),
                "GOEXE", Os.getHostOs().exeExtension(),
                "GOARCH", Arch.getHostArch().toString());
        defaultEnvs.putAll(env);
        return defaultEnvs;
    }

    private String joinGopaths() {
        String joinedGoPaths = gopaths.stream()
                .map(Path::toString)
                .collect(Collectors.joining(":")); // "John, Anna, Paul
        return joinedGoPaths;
    }

    private String getGoBinary() {
        return toUnixString(goBinaryManager.getBinaryPath().toAbsolutePath());
    }


    public String getGopath() {
        return gopath;
    }

    @Override
    public List<Path> getGopaths() {
        return gopaths;
    }
}
