package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;

public interface DependencyManager {

    String CURRENT_VERSION_INDICATOR_FILE = ".CURRENT_VERSION";

    /**
     * Resolves a dependency.
     * During this process, right version will be determined by VCS.
     *
     * @param context    the configuration this dependency in and current exclusion specs
     * @param dependency dependency to be resolved
     * @return the resolved dependency
     */
    ResolvedDependency resolve(ResolveContext context, NotationDependency dependency);


    /**
     * Copy all necessary files of this {@code dependency} to the {@code targetDirectory}.
     *
     * @param dependency      the dependency
     * @param targetDirectory the target directory
     */
    void install(ResolvedDependency dependency, File targetDirectory);
}
