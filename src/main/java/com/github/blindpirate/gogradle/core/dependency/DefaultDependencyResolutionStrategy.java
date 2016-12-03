package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

import java.util.Map;

public class DefaultDependencyResolutionStrategy implements DependencyResolutionStrategy {

    @Override
    public boolean required(GolangPackageModule module, DependencyType type) {
        return type == DependencyType.Vendor;
    }

    @Override
    public GolangDependencySet resolve(Map<DependencyType, GolangDependencySet> dependencySets) {
        return dependencySets.get(DependencyType.Vendor);
    }
}
