package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class ProjectCacheManager {
    private final BuildScopedVcsNotationCache buildScopedVcsNotationCache;
    private final ConcreteVcsNotationToResolvedDependencyCache concreteVcsNotationToResolvedDependencyCache;
    private final ResolveDependencyToDependenciesCache resolveDependencyToDependenciesCache;

    @Inject
    public ProjectCacheManager(BuildScopedVcsNotationCache buildScopedNotationCache,
                               ConcreteVcsNotationToResolvedDependencyCache concreteNotationToResolvedDependencyCache,
                               ResolveDependencyToDependenciesCache resolveDependencyToDependenciesCache) {
        this.buildScopedVcsNotationCache = buildScopedNotationCache;
        this.concreteVcsNotationToResolvedDependencyCache = concreteNotationToResolvedDependencyCache;
        this.resolveDependencyToDependenciesCache = resolveDependencyToDependenciesCache;
    }

    public void loadPersistenceCache() {
        concreteVcsNotationToResolvedDependencyCache.load();
        resolveDependencyToDependenciesCache.load();
    }

    public void savePersistenceCache() {
        concreteVcsNotationToResolvedDependencyCache.save();
        resolveDependencyToDependenciesCache.save();
    }

    public ResolvedDependency resolve(NotationDependency notationDependency,
                                      Function<NotationDependency, ResolvedDependency> constructor) {
        if (notationDependency.isConcrete()) {
            return concreteVcsNotationToResolvedDependencyCache.get(notationDependency, constructor);
        } else {
            return buildScopedVcsNotationCache.get(notationDependency, constructor);
        }
    }

    public GolangDependencySet produce(ResolvedDependency resolvedDependency,
                                       Function<ResolvedDependency, GolangDependencySet> constructor) {
        return resolveDependencyToDependenciesCache.get(resolvedDependency, constructor);
    }
}
