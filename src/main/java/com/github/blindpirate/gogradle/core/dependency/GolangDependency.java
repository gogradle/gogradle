package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackage;
import org.gradle.api.artifacts.Dependency;

import java.io.Serializable;

/**
 * A {@link GolangDependency} represents a dependency
 * such as a specific version of source code or a local directory.
 */
public interface GolangDependency extends Dependency, Serializable {
    /**
     * The dependency's import path, e.g., golang.org/x/crypto/cmd.
     * <p>
     * However, currently we support golang.org/x/crypto (the root path) only.
     *
     * @return dependency's import path
     */
    @Override
    String getName();

    /**
     * A unique identifier to locate a dependency, e.g., git commit id.
     *
     * @return the version string
     */
    @Override
    String getVersion();

    GolangPackage getPackage();

    ResolvedDependency resolve(ResolveContext context);

    boolean isFirstLevel();


}

