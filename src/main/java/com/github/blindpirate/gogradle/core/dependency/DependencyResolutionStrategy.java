package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

/**
 * Direct how to merge vendor dependency, source dependency, and external dependency
 */
public interface DependencyResolutionStrategy {
    boolean needVendorDependencies(GolangPackageModule module);

    boolean needSourceDependencies(GolangPackageModule module);

    boolean needExternalDependencies(GolangPackageModule module);

    GolangDependencySet resolve(GolangDependencySet vendorDependencies,
                                GolangDependencySet sourceDependencies,
                                GolangDependencySet externalDependencies);
}
