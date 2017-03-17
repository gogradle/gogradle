package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.github.blindpirate.gogradle.util.MapUtils;
import groovy.lang.Closure;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Task;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GoBuildTask extends Go {

    @Inject
    private GolangPluginSetting setting;

    public GoBuildTask() {
        dependsOn(GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME);
    }

    @Override
    protected void doAddDefaultAction() {
        getEnvs().forEach(this::addOneBuildAction);
    }

    private void addOneBuildAction(Map<String, String> env) {
        List<String> args = Arrays.asList("build", "-o", "./.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}");
        doLast(task -> buildManager.go(args, env));
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
        List<Map<String, String>> ret = setting.getTargetPlatforms().stream()
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
