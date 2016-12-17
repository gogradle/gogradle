package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.produce.DependencyProduceStrategy;
import org.gradle.api.artifacts.Dependency;

public abstract class AbstractGolangDependency implements GolangDependency {

    private String name;

    private boolean firstLevel = false;

    private DependencyProduceStrategy strategy = DependencyProduceStrategy.DEFAULT_STRATEGY;

    @Override
    public boolean isFirstLevel() {
        return firstLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DependencyProduceStrategy getProduceStrategy() {
        return strategy;
    }

    public AbstractGolangDependency setName(String name) {
        this.name = name;
        return this;
    }

    public AbstractGolangDependency setFirstLevel(boolean firstLevel) {
        this.firstLevel = firstLevel;
        return this;
    }

    public AbstractGolangDependency setStrategy(DependencyProduceStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

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
