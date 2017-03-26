package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.Go;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import groovy.lang.Closure;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Task;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class GoBuildTask extends Go {
    private static final Pattern TARGET_PLATFORM_PATTERN
            = Pattern.compile("(\\s*\\w+\\-\\w+\\s*)(,\\s*\\w+\\-\\w+\\s*)*");

    private List<Pair<Os, Arch>> targetPlatforms = asList(Pair.of(Os.getHostOs(), Arch.getHostArch()));

    private String outputLocation = "./.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}";

    public void setTargetPlatform(String targetPlatform) {
        Matcher matcher = TARGET_PLATFORM_PATTERN.matcher(targetPlatform);
        Assert.isTrue(matcher.matches(),
                "Illegal target platform:" + targetPlatform);
        targetPlatforms = extractPlatforms(targetPlatform);
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    private List<Pair<Os, Arch>> extractPlatforms(String targetPlatform) {
        String[] platforms = StringUtils.splitAndTrim(targetPlatform, ",");
        return Stream.of(platforms).map(this::extractOne).collect(toList());
    }

    private Pair<Os, Arch> extractOne(String osAndArch) {
        String[] osArch = StringUtils.splitAndTrim(osAndArch, "\\-");
        Os os = Os.of(osArch[0]);
        Arch arch = Arch.of(osArch[1]);
        return Pair.of(os, arch);
    }

    public GoBuildTask() {
        dependsOn(GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
    }

    @Override
    protected void doAddDefaultAction() {
        doLast(task -> getEnvs().forEach(this::buildWithEnv));
    }

    private void buildWithEnv(Map<String, String> env) {
        setEnv(env);
        go(Arrays.asList("build", "-o", outputLocation));
    }


    @Override
    public Task doLast(final Closure closure) {
        List<Map<String, String>> envs = getEnvs();
        envs.forEach(env -> doLast(GoExecutionAction.wrapClosureWithEnvs(closure, env)));
        return this;
    }

    @Override
    public Task doFirst(final Closure closure) {
        List<Map<String, String>> envs = getEnvs();
        envs.forEach(env -> doFirst(GoExecutionAction.wrapClosureWithEnvs(closure, env)));
        return this;
    }

    public Task leftShift(final Closure action) {
        throw new UnsupportedOperationException("Left shift is not supported since it's deprecated officially");
    }

    private List<Map<String, String>> getEnvs() {
        List<Map<String, String>> ret = targetPlatforms.stream()
                .map(this::pairToEnv)
                .collect(Collectors.toList());
        ret.forEach(env -> env.put("GOPATH", buildManager.getBuildGopath()));
        return ret;
    }

    private Map<String, String> pairToEnv(Pair<Os, Arch> osAndArches) {
        return MapUtils.asMap("GOOS", osAndArches.getLeft().toString(),
                "GOARCH", osAndArches.getRight().toString(),
                "GOEXE", osAndArches.getLeft().exeExtension());
    }
}
