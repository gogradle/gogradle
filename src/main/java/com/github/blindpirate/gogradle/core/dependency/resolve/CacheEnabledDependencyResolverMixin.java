package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public interface CacheEnabledDependencyResolverMixin extends DependencyResolver {
    default ResolvedDependency resolve(ResolveContext context, NotationDependency dependency) {
        return getProjectCacheManager()
                .resolve(dependency, notationDependency -> doResolve(context, notationDependency));
    }

    ProjectCacheManager getProjectCacheManager();

    ResolvedDependency doResolve(ResolveContext context, NotationDependency dependency);
}
