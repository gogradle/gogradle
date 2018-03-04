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

package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.common.GoSourceCodeFilter;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.common.GoSourceCodeFilter.SourceSetType.PROJECT_AND_VENDOR_BUILD_FILES;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.util.StringUtils.capitalizeFirstLetter;
import static com.github.blindpirate.gogradle.util.StringUtils.splitAndTrim;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class GoBuild extends Go {
    @Inject
    private GolangPluginSetting setting;
    @Inject
    private GoBinaryManager binaryManager;

    private static final Pattern TARGET_PLATFORMS_PATTERN = Pattern.compile("(\\s*\\w+-\\w+\\s*)(,\\s*\\w+-\\w+\\s*)*");
    private static final Pattern TARGET_PLATFORM_PATTERN = Pattern.compile("\\w+-\\w+");
    private static final String DEFAULT_OUTPUT_LOCATION = "./.gogradle/${PROJECT_NAME}-${GOOS}-${GOARCH}";

    private String outputLocation;

    private List<Pair<Os, Arch>> targetPlatforms = singletonList(Pair.of(Os.getHostOs(), Arch.getHostArch()));

    public GoBuild() {
        setDescription("Run build and generate output.");
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    @Override
    public void afterEvaluate() {
        targetPlatforms.forEach(osArchPair -> {
            Os os = osArchPair.getLeft();
            Arch arch = osArchPair.getRight();

            Go task = createSingleGoTask(os, arch);
            task.dependsOn(INSTALL_DEPENDENCIES_TASK_NAME, RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
            configureSubTask(task, os, arch);
        });

        if (!this.goActions.isEmpty()) {
            this.goActions.clear();
        }
    }

    private boolean noCustomActions() {
        return goActions.isEmpty();
    }

    private void configureSubTask(Go subTask, Os os, Arch arch) {
        configureStdoutStderr(subTask);
        subTask.setContinueOnFailure(continueOnFailure);
        configureActions(subTask);
        configureEnvironment(os, arch, subTask);
        configureInputsOutputs(subTask, os, arch);
        this.dependsOn(subTask);
    }

    private void configureInputsOutputs(Go subTask, Os os, Arch arch) {
        if (noCustomActions() || outputLocation != null) {
            subTask.getInputs().files((Callable<Collection<File>>)
                    () -> GoSourceCodeFilter.filterGoFiles(getProjectDir(), PROJECT_AND_VENDOR_BUILD_FILES));
            subTask.getInputs().property("buildTags", (Callable<List<String>>) () -> setting.getBuildTags());
            subTask.getInputs().property("goVersion", (Callable<String>) () -> binaryManager.getGoVersion());
            subTask.getInputs().property("goEnv", getEnv(os, arch));
            subTask.getOutputs().file(new File(getProjectDir(), getOutputLocation()));
        }
    }

    public String getOutputLocation() {
        return outputLocation == null ? DEFAULT_OUTPUT_LOCATION : outputLocation;
    }

    private void configureStdoutStderr(Go subTask) {
        subTask.stdout(this.stdoutLineConsumer);
        subTask.stderr(this.stderrLineConsumer);
    }

    private void configureActions(Go subTask) {
        if (noCustomActions()) {
            subTask.go(Arrays.asList("build", "-o", getOutputLocation(), setting.getPackagePath()));
        } else {
            goActions.forEach(subTask::addGoAction);
        }
    }

    private void configureEnvironment(Os os, Arch arch, Go task) {
        Map<String, String> env = getEnv(os, arch);
        env.putAll(this.environment);
        task.environment(env);
    }

    private Go createSingleGoTask(Os os, Arch arch) {
        String taskName = "build"
                + capitalizeFirstLetter(os.toString())
                + capitalizeFirstLetter(arch.toString());
        return getProject().getTasks().create(taskName, Go.class);
    }

    @Deprecated
    public void setTargetPlatform(String targetPlatform) {
        Matcher matcher = TARGET_PLATFORMS_PATTERN.matcher(targetPlatform);
        Assert.isTrue(matcher.matches(),
                "Illegal target platform:" + targetPlatform);
        targetPlatforms = extractPlatforms(targetPlatform);
        removeDuplicates();
    }

    public void setTargetPlatform(List<String> targetPlatformList) {
        Assert.isNotEmpty(targetPlatformList, "Target platform cannot be empty!");
        Assert.isTrue(targetPlatformList.stream().allMatch(s -> TARGET_PLATFORM_PATTERN.matcher(s).matches()),
                "Illegal target platform:" + targetPlatformList);
        targetPlatforms = targetPlatformList.stream().map(this::extractOne).collect(toList());
        removeDuplicates();
    }

    private void removeDuplicates() {
        targetPlatforms = new ArrayList<>(new LinkedHashSet<>(targetPlatforms));
    }

    private List<Pair<Os, Arch>> extractPlatforms(String targetPlatform) {
        String[] platforms = splitAndTrim(targetPlatform, ",");
        return Stream.of(platforms).map(this::extractOne).collect(toList());
    }

    private Pair<Os, Arch> extractOne(String osAndArch) {
        String[] osArch = splitAndTrim(osAndArch, "\\-");
        Os os = Os.of(osArch[0]);
        Arch arch = Arch.of(osArch[1]);
        return Pair.of(os, arch);
    }

    private Map<String, String> getEnv(Os os, Arch arch) {
        return MapUtils.asMap("GOOS", os.toString(),
                "GOARCH", arch.toString(),
                "GOEXE", os.exeExtension());
    }
}
