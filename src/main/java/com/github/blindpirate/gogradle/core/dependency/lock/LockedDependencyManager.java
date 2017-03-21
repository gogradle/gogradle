package com.github.blindpirate.gogradle.core.dependency.lock;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.util.Collection;

public interface LockedDependencyManager {
    GolangDependencySet getLockedDependencies(String configuration);

    void lock(Collection<? extends ResolvedDependency> flatBuildDependencies,
              Collection<? extends ResolvedDependency> flatTestDependencies);
}
