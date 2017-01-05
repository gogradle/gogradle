package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.task.BuildTask;
import com.github.blindpirate.gogradle.core.task.DependenciesTask;
import com.github.blindpirate.gogradle.core.task.PrepareTask;
import com.github.blindpirate.gogradle.core.task.ResolveTask;
import com.github.blindpirate.gogradle.core.task.TestTask;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.Task;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class GolangTaskContainer {
    // prepare everything
    public static final String PREPARE_TASK_NAME = "prepare";
    // produce all dependencies by analyzing build.gradle
    public static final String RESOLVE_TASK_NAME = "resolve";
    // show dependencies tree
    public static final String DEPENDENCIES_TASK_NAME = "dependencies";

    public static final String CHECK_TASK_NAME = "check";
    public static final String BUILD_TASK_NAME = "build";
    public static final String CLEAN_TASK_NAME = "clean";
    public static final String INSTALL_TASK_NAME = "install";
    public static final String TEST_TASK_NAME = "test";
    public static final String COVERAGE_CHECK_TASK_NAME = "coverageCheck";

    public static final Map<String, Class<? extends Task>> TASKS = ImmutableMap.<String, Class<? extends Task>>builder()
            .put(PREPARE_TASK_NAME, PrepareTask.class)
            .put(RESOLVE_TASK_NAME, ResolveTask.class)
            .put(DEPENDENCIES_TASK_NAME, DependenciesTask.class)
            .put(BUILD_TASK_NAME, BuildTask.class)
            .put(TEST_TASK_NAME, TestTask.class)
            .build();

    private Map<Class<? extends Task>, Task> tasks = new HashMap<>();

    public <T extends Task> void put(Class<T> clazz, T task) {
        this.tasks.put(clazz, task);
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> T get(Class<T> clazz) {
        return (T) this.tasks.get(clazz);
    }
}
