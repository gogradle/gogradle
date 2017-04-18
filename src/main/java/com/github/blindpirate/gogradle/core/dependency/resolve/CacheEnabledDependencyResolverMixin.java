package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.concurrent.atomic.AtomicBoolean;

public interface CacheEnabledDependencyResolverMixin extends DependencyResolver {
    Logger LOGGER = Logging.getLogger(CacheEnabledDependencyResolverMixin.class);

    default ResolvedDependency resolve(ResolveContext context, NotationDependency dependency) {
        AtomicBoolean functionInvoked = new AtomicBoolean(false);

        ResolvedDependency ret = getProjectCacheManager()
                .resolve(dependency, notationDependency -> {
                    functionInvoked.set(true);
                    return doResolve(context, notationDependency);
                });

        if (functionInvoked.get()) {
            LOGGER.quiet("Resolving {}", dependency);
        } else {
            LOGGER.quiet("Resolving cached {}", dependency);
        }

        return ret;
    }

    ProjectCacheManager getProjectCacheManager();

    ResolvedDependency doResolve(ResolveContext context, NotationDependency dependency);
}
