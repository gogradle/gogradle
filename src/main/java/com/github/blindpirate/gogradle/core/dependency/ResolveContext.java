package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy;

import java.io.File;
import java.util.Set;
import java.util.function.Predicate;

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
                    NotationDependency.class.cast(dependency).getTransitiveDepExclusions());
        } else {
            return new ResolveContext(this, configuration, emptySet());
        }
    }

    public static ResolveContext root(GolangConfiguration configuration,
                                      DependencyProduceStrategy strategy) {
        return new ResolveContext(null, configuration, emptySet(), strategy);
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

    private ResolveContext(ResolveContext parent,
                           GolangConfiguration configuration,
                           Set<Predicate<GolangDependency>> transitiveDepExclusions) {
        this(parent, configuration, transitiveDepExclusions, DependencyProduceStrategy.DEFAULT_STRATEGY);
    }

    public GolangDependencySet produceTransitiveDependencies(ResolvedDependency dependency,
                                                             File rootDir) {
        DependencyVisitor visitor = GogradleGlobal.getInstance(DependencyVisitor.class);
        GolangDependencySet ret = dependencyProduceStrategy.produce(dependency,
                rootDir,
                visitor,
                configuration.getName());

        return filterRecursively(ret);
    }

    private GolangDependencySet filterRecursively(GolangDependencySet set) {
        GolangDependencySet ret = set.stream()
                .filter(this::shouldBeReserved)
                .collect(GolangDependencySet::new, GolangDependencySet::add, GolangDependencySet::addAll);

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
