package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy;

import java.io.File;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy.DEFAULT_STRATEGY;
import static java.util.Collections.emptySet;

public class ResolveContext {

    private ResolveContext parent;

    private GolangConfiguration configuration;

    private DependencyProduceStrategy dependencyProduceStrategy;

    private Set<Predicate<GolangDependency>> transitiveDepExclusions;

    public GolangConfiguration getConfiguration() {
        return configuration;
    }

    public DependencyRegistry getDependencyRegistry() {
        return configuration.getDependencyRegistry();
    }

    public ResolveContext createSubContext(GolangDependency dependency) {
        if (dependency instanceof NotationDependency) {
            return new ResolveContext(this,
                    configuration,
                    NotationDependency.class.cast(dependency).getTransitiveDepExclusions(),
                    dependencyProduceStrategy);
        } else {
            return new ResolveContext(this, configuration, emptySet(), dependencyProduceStrategy);
        }
    }

    public static ResolveContext root(GolangConfiguration configuration) {
        return new ResolveContext(null, configuration, emptySet(), DEFAULT_STRATEGY);
    }

    private ResolveContext(ResolveContext parent,
                           GolangConfiguration configuration,
                           Set<Predicate<GolangDependency>> transitiveDepExclusions,
                           DependencyProduceStrategy strategy) {
        this.parent = parent;
        this.configuration = configuration;
        this.transitiveDepExclusions = transitiveDepExclusions;
        this.dependencyProduceStrategy = strategy;
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
}
