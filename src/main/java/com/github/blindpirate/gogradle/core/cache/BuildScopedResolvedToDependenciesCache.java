package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Singleton;

@Singleton
public class BuildScopedResolvedToDependenciesCache
        extends AbstractCache<ResolvedDependency, GolangDependencySet> {
}
