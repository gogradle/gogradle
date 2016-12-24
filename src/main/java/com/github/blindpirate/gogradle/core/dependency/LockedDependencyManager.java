package com.github.blindpirate.gogradle.core.dependency;

import java.util.Optional;

public interface LockedDependencyManager {
    Optional<GolangDependencySet> getLockedDependencies();

    void lock(GolangDependencySet flatDependencies);
}
