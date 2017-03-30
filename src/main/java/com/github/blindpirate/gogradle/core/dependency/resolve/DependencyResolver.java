package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public interface DependencyResolver {
    /**
     * Resolves a dependency.
     * During this process, right version will be determined by VCS.
     *
     * @param configuration configuration this dependency in
     * @param dependency dependency to be resolved
     * @return the resolved dependency
     */
    ResolvedDependency resolve(GolangConfiguration configuration, NotationDependency dependency);
}
