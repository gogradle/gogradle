/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy.DEFAULT_STRATEGY;

public class ResolveContext {

    private ResolveContext parent;

    private GolangConfiguration configuration;

    private DependencyProduceStrategy dependencyProduceStrategy;

    private GolangDependency dependency;

    private Set<Predicate<GolangDependency>> transitiveDepExclusions = Collections.emptySet();

    public GolangConfiguration getConfiguration() {
        return configuration;
    }

    public DependencyRegistry getDependencyRegistry() {
        return configuration.getDependencyRegistry();
    }

    public ResolveContext createSubContext(GolangDependency dependency) {
        return new ResolveContext(this, configuration, dependency, dependencyProduceStrategy);
    }

    public static ResolveContext root(GolangDependency rootDependency, GolangConfiguration configuration) {
        return new ResolveContext(null, configuration, rootDependency, DEFAULT_STRATEGY);
    }

    private ResolveContext(ResolveContext parent,
                           GolangConfiguration configuration,
                           GolangDependency dependency,
                           DependencyProduceStrategy strategy) {
        this.parent = parent;
        this.configuration = configuration;
        this.dependencyProduceStrategy = strategy;
        this.dependency = dependency;

        if (dependency instanceof NotationDependency) {
            this.transitiveDepExclusions = NotationDependency.class.cast(dependency).getTransitiveDepExclusions();
        }
    }


    public GolangDependencySet produceTransitiveDependencies(ResolvedDependency dependency,
                                                             File rootDir) {
        DependencyVisitor visitor = GogradleGlobal.getInstance(DependencyVisitor.class);
        ProjectCacheManager projectCacheManager = GogradleGlobal.getInstance(ProjectCacheManager.class);

        GolangDependencySet ret = projectCacheManager.produce(dependency,
                resolvedDependency -> dependencyProduceStrategy.produce(dependency, rootDir, visitor, BUILD));

        return filterRecursively(ret);
    }

    private GolangDependencySet filterRecursively(GolangDependencySet set) {
        GolangDependencySet ret = set.stream()
                .filter(this::shouldBeReserved)
                .collect(GolangDependencySet.COLLECTOR);

        ret.forEach(dependency -> {
            if (dependency instanceof VendorResolvedDependency) {
                VendorResolvedDependency vendorResolvedDependency = (VendorResolvedDependency) dependency;
                vendorResolvedDependency.setDependencies(filterRecursively(vendorResolvedDependency.getDependencies()));
            }
        });
        return ret;
    }

    private boolean shouldBeReserved(GolangDependency dependency) {
        ResolveContext current = this;
        while (current != null) {
            if (current.transitiveDepExclusions
                    .stream().anyMatch(predicate -> predicate.test(dependency))) {
                return false;
            }
            current = current.parent;
        }
        return true;
    }

    public GolangDependency getDependency() {
        return dependency;
    }

    public ResolveContext getParent() {
        return parent;
    }
}
