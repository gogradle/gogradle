package com.github.blindpirate.gogradle.core.dependency;

import org.gradle.api.specs.Spec;

import java.util.Set;

public interface NotationDependency extends GolangDependency {
    Set<Spec<GolangDependency>> getTransitiveDepExclusions();
}
