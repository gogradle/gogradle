package com.github.blindpirate.gogradle.core.dependency;

/**
 * Manages global dependency register.
 * When a dependency is resolved, its repo information will be registered here.
 * Later, resolving dependency of same repo can be faster.
 */
public interface DependencyRegistry {
    /**
     * Registers a dependency
     *
     * @param dependency
     * @return true if the dependency is newer than existing dependency with same name.
     */
    boolean register(ResolvedDependency dependency);

    ResolvedDependency retrive(String name);
}
