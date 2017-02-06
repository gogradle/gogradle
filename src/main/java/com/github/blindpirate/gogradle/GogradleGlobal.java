package com.github.blindpirate.gogradle;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.gradle.api.Project;

public enum GogradleGlobal {
    INSTANCE;

    public static final String GOGRADLE_VERSION = "0.1.1";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String GOGRADLE_BUILD_DIR_NAME = ".gogradle";
    public static final int MAX_DFS_DEPTH = 100;

    private Boolean offline;

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
        if (INSTANCE.offline == null) {
            Project project = INSTANCE.getInstance(Project.class);
            INSTANCE.offline = project.getGradle().getStartParameter().isOffline();
        }
        return INSTANCE.offline;
    }

    public static <T> T getInstance(Key<T> key) {
        return INSTANCE.getInjector().getInstance(key);
    }
}
