package com.github.blindpirate.gogradle.task;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;

public class ResolveBuildDependenciesTask extends ResolveTask {
    @Override
    protected String getConfigurationName() {
        return BUILD;
    }
}
