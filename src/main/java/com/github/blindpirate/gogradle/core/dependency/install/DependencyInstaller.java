package com.github.blindpirate.gogradle.core.dependency.install;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;

public interface DependencyInstaller {

    String CURRENT_VERSION_INDICATOR_FILE = ".CURRENT_VERSION";

    /**
     * Copy all necessary files of this {@code dependency} to the {@code targetDirectory}.
     *
     * @param dependency      the dependency
     * @param targetDirectory the target directory
     */
    void install(ResolvedDependency dependency, File targetDirectory);
}
