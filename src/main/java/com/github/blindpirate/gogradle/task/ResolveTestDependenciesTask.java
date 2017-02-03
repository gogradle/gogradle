package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.Configuration;

public class ResolveTestDependenciesTask extends ResolveTask {
    @Override
    protected Configuration getConfiguration() {
        return Configuration.TEST;
    }
}
