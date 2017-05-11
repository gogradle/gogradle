package com.github.blindpirate.gogradle.core.dependency.lock;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;
import java.util.Collection;

public interface LockedDependencyManager {
    void lock(Collection<? extends ResolvedDependency> flatBuildDependencies,
              Collection<? extends ResolvedDependency> flatTestDependencies);

    GolangDependencySet produce(File rootDir, String configuration);

    boolean canRecognize(File rootDir);
}
