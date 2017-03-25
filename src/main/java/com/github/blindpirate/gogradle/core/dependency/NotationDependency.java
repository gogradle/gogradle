package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy;
import org.gradle.api.specs.Spec;

import java.util.Set;

public interface NotationDependency extends GolangDependency {
    GolangPackage getPackage();

    DependencyProduceStrategy getStrategy();

    Set<Spec<GolangDependency>> getTransitiveDepExclusions();
}
