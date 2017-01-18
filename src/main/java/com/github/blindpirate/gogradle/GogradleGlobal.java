package com.github.blindpirate.gogradle;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.gradle.api.Project;

public enum GogradleGlobal {
    INSTANCE;

    public static final String GOGRADLE_VERSION = "0.1.0";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int MAX_DFS_DEPTH = 100;


    private Injector injector;

    public Injector getInjector() {
        return injector;
    }

    void setInjector(Injector injector) {
        this.injector = injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        return INSTANCE.injector.getInstance(clazz);
    }

    public static boolean isOffline() {
        Project project = INSTANCE.getInstance(Project.class);
        return project.getGradle().getStartParameter().isOffline();
    }

    public static <T> T getInstance(Key<T> key) {
        return INSTANCE.getInjector().getInstance(key);
    }
}
