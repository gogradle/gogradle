package com.github.blindpirate.gogradle.core.dependency;

/**
 * Manages global dependency registry.
 * When a dependency is resolved, its repo information will be registered here.
 * Later, resolving dependency of same repo can be faster.
 */
public interface DependencyRegistry {
    boolean shouldBeIgnore(GolangDependency dependency);
}
