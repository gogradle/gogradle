package com.github.blindpirate.gogradle.task;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME;

public class InstallTestDependenciesTask extends InstallTask {
    public InstallTestDependenciesTask() {
        dependsOn(RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
    }

    @Override
    protected ResolveTask getUpstreamResolveTask() {
        return getTask(ResolveTestDependenciesTask.class);
    }
}
