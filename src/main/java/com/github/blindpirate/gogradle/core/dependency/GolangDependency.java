package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyProduceStrategy;
import org.gradle.api.artifacts.Dependency;

/**
 * A {@link GolangDependency} represents a dependency
 * such as a specific version of source code or a local directory.
 */
public interface GolangDependency extends Dependency {
    /**
     * Get the package, it may be a package existing on disk or just a proxy.
     *
     * @return The package specified by this dependency
     */
    GolangPackageModule getPackage();

    /**
     * Indicates how to produce dependencies of a package, by scanning its code or using its vendor?
     *
     * @return
     */
    DependencyProduceStrategy getProduceStrategy();

    /**
     * Dependencies in root project (including vendor or build.gradle)
     * have higher priority than transitive dependencies.
     *
     * @return
     */
    boolean isFirstLevel();

    final class Namer implements org.gradle.api.Namer<GolangDependency> {

        public static final Namer INSTANCE = new Namer();

        private Namer() {
        }

        @Override
        public String determineName(GolangDependency dependency) {
            return dependency.getName();
        }
    }

}

