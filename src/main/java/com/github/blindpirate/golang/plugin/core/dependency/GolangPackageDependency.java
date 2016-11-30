package com.github.blindpirate.golang.plugin.core.dependency;

import org.gradle.api.artifacts.Dependency;

public abstract class GolangPackageDependency implements Dependency {

    private String name;

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        return false;
    }

    @Override
    public Dependency copy() {
        return null;
    }
}
