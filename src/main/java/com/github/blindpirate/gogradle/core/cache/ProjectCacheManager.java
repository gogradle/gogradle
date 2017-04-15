package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class ProjectCacheManager {
    private final BuildScopedNotationCache buildScopedNotationCache;
    private final ConcreteNotationToResolvedDependencyCache concreteNotationToResolvedDependencyCache;
    private final ResolveDependencyToDependenciesCache resolveDependencyToDependenciesCache;

    @Inject
    public ProjectCacheManager(BuildScopedNotationCache buildScopedNotationCache,
                               ConcreteNotationToResolvedDependencyCache concreteNotationToResolvedDependencyCache,
                               ResolveDependencyToDependenciesCache resolveDependencyToDependenciesCache) {
        this.buildScopedNotationCache = buildScopedNotationCache;
        this.concreteNotationToResolvedDependencyCache = concreteNotationToResolvedDependencyCache;
        this.resolveDependencyToDependenciesCache = resolveDependencyToDependenciesCache;
    }

    public void loadPersistenceCache() {
        concreteNotationToResolvedDependencyCache.load();
        resolveDependencyToDependenciesCache.load();
    }

    public void savePersistenceCache() {
        concreteNotationToResolvedDependencyCache.save();
        resolveDependencyToDependenciesCache.save();
    }

    public ResolvedDependency resolve(NotationDependency notationDependency,
                                      Function<NotationDependency, ResolvedDependency> constructor) {
        if (notationDependency.isConcrete()) {
            return concreteNotationToResolvedDependencyCache.get(notationDependency, constructor);
        } else {
            return buildScopedNotationCache.get(notationDependency, constructor);
        }
    }

    public GolangDependencySet produce(ResolvedDependency resolvedDependency,
                                       Function<ResolvedDependency, GolangDependencySet> constructor) {
        return resolveDependencyToDependenciesCache.get(resolvedDependency, constructor);
    }
}
