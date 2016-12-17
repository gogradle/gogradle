package com.github.blindpirate.gogradle.core.dependency;

import com.google.common.base.Optional;

public interface LockedDependencyManager {
    Optional<GolangDependencySet> getLockedDependencies();

    void lock(GolangDependencySet flatDependencies);
}
