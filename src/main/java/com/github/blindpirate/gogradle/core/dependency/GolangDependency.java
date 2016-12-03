package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import org.gradle.api.artifacts.Dependency;

/**
 * A {@link GolangDependency} represents a dependency
 * such as a specific version of source code or a local directory.
 */
public interface GolangDependency extends Dependency {
    /**
     * Get the package which may be just a proxy.
     *
     * @return The package specified by this dependency
     */
    GolangPackageModule getPackage();
}

