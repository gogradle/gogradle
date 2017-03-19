package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangConfiguration;
import org.gradle.api.artifacts.Dependency;

/**
 * A {@link GolangDependency} represents a dependency
 * such as a specific version of source code or a local directory.
 */
public interface GolangDependency extends Dependency {
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

    ResolvedDependency resolve(GolangConfiguration configuration);

    boolean isFirstLevel();

    enum Namer implements org.gradle.api.Namer<GolangDependency> {

        INSTANCE;

        @Override
        public String determineName(GolangDependency dependency) {
            return dependency.getName();
        }
    }

}

