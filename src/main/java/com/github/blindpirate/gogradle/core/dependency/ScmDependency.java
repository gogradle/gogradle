package com.github.blindpirate.gogradle.core.dependency;

import org.gradle.api.artifacts.Dependency;

public abstract class ScmDependency implements GolangDependency {
    private String name;

    public ScmDependency(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        return null;
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
