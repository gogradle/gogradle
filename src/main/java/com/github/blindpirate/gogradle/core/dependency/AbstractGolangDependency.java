package com.github.blindpirate.gogradle.core.dependency;

import org.gradle.api.artifacts.Dependency;

public abstract class AbstractGolangDependency implements GolangDependency {

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dependency copy() {
        throw new UnsupportedOperationException();
    }
}
