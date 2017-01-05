package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.GolangTaskContainer;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;

import javax.inject.Inject;

public class AbstractGolangTask extends DefaultTask {
    @Inject
    private GolangTaskContainer golangTaskContainer;

    protected <T extends Task> T getTask(Class<T> clazz) {
        return golangTaskContainer.get(clazz);
    }
}
