package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;

public class ResolveTestDependenciesTask extends ResolveTask {
    public ResolveTestDependenciesTask() {
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
    }

    @Override
    public String getConfigurationName() {
        return TEST;
    }

    @Override
    protected GolangDependencySet produceFirstLevelDependencies() {
        GolangDependencySet ret = super.produceFirstLevelDependencies();
        // this is complete build dependencies
        GolangDependencySet flatDeps = getTask(ResolveBuildDependenciesTask.class).getDependencyTree().flatten();
        ret.removeAll(flatDeps);
        return ret;
    }
}
