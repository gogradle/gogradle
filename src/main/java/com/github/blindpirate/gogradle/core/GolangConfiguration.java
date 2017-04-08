package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;

public class GolangConfiguration {

    public static final String BUILD = "build";
    public static final String TEST = "test";

    private final String name;
    private final GolangDependencySet dependencies = new GolangDependencySet();
    private final DependencyRegistry dependencyRegistry = new DefaultDependencyRegistry();

    public GolangConfiguration(String name) {
        this.name = name;
    }

    public DependencyRegistry getDependencyRegistry() {
        return dependencyRegistry;
    }

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public String getName() {
        return name;
    }
}
