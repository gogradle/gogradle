package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

import java.util.Map;

/**
 * Direct how to merge vendor dependency, source dependency, and external dependency
 */
public interface DependencyResolutionStrategy {

    enum DependencyType {
        Vendor,
        Source,
        External
    }

    boolean required(GolangPackageModule module, DependencyType type);

    GolangDependencySet resolve(Map<DependencyType, GolangDependencySet> dependencySets);
}
