package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public interface DependencyResolver {
    /**
     * Resolves a dependency.
     * During this process, right version will be determined by VCS.
     *
     * @param context the configuration this dependency in and current exclusion specs
     * @param dependency    dependency to be resolved
     * @return the resolved dependency
     */
    ResolvedDependency resolve(ResolveContext context, NotationDependency dependency);
}
