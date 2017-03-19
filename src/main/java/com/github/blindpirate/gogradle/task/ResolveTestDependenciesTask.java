package com.github.blindpirate.gogradle.task;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;

public class ResolveTestDependenciesTask extends ResolveTask {
    @Override
    protected String getConfigurationName() {
        return TEST;
    }
}
