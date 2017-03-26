package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackage;
import org.gradle.api.specs.Spec;

import java.util.Set;

public interface NotationDependency extends GolangDependency {
    GolangPackage getPackage();

    Set<Spec<GolangDependency>> getTransitiveDepExclusions();
}
