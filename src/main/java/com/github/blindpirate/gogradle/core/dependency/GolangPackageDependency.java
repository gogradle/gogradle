package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

/**
 * A {@link GolangPackageDependency} represents a dependency
 * such as a specific version of source code or a local directory.
 */
public interface GolangPackageDependency {
    /**
     * Get the package which may be just a proxy.
     *
     * @return The package specified by this dependency
     */
    GolangPackageModule getPackage();
}

