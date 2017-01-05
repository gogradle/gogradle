package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import org.gradle.api.specs.Spec;

import java.util.Set;

public interface NotationDependency extends GolangDependency {

    DependencyProduceStrategy getStrategy();

    Set<Spec<GolangDependency>> getTransitiveDepExclusions();

    Class<? extends DependencyResolver> getResolverClass();
}
