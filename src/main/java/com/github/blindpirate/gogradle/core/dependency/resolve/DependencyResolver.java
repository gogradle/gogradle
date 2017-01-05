package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;

public interface DependencyResolver {
    /**
     * Resolves a dependency.
     * During this process, right version will be determined by VCS.
     *
     * @param dependency dependency to be resolved
     * @return the resolved dependency
     */
    ResolvedDependency resolve(NotationDependency dependency);

    /**
     * Copy all necessary files of this {@code dependency} to the {@code targetLocation}.
     */

    void reset(ResolvedDependency dependency, File targetDirectory);
}
