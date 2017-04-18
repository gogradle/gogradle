package com.github.blindpirate.gogradle.task;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;

public class InstallBuildDependenciesTask extends InstallTask {

    public InstallBuildDependenciesTask() {
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
    }

    @Override
    protected ResolveTask getUpstreamResolveTask() {
        return getTask(ResolveBuildDependenciesTask.class);
    }
}
