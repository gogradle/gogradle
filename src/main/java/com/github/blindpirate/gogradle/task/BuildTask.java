package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class BuildTask extends AbstractGolangTask {

    @Inject
    private BuildManager buildManager;

    @TaskAction
    public void build() {
        buildManager.build();
    }
}
