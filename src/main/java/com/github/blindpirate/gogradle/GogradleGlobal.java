package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.util.Assert;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.gradle.api.Project;

public enum GogradleGlobal {

    INSTANCE;

    private Injector injector;

    public Injector getInjector() {
        return injector;
    }

    void setInjector(Injector injector) {
        this.injector = injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        Assert.isNotNull(INSTANCE.injector);
        return INSTANCE.injector.getInstance(clazz);
    }

    public static boolean isOffline() {
        Project project = INSTANCE.getInstance(Project.class);
        return project.getGradle().getStartParameter().isOffline();
    }

    public static <T> T getInstance(Key<T> key) {
        Assert.isNotNull(INSTANCE.injector);
        return INSTANCE.getInjector().getInstance(key);
    }
}
