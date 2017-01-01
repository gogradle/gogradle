package com.github.blindpirate.gogradle.core.dependency.lock;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;

import java.util.Collection;

public interface LockedDependencyManager {
    GolangDependencySet getLockedDependencies();

    void lock(Collection<? extends GolangDependency> flatDependencies);
}
