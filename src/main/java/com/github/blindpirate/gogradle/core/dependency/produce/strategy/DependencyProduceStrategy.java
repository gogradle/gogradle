package com.github.blindpirate.gogradle.core.dependency.produce.strategy;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;

import java.io.File;

/**
 * Direct how to generate dependencies of an existing golang package module.
 */
// ExternalOnly/VendorOnly/SourceCodeOnly/GogradleDevelop/GogradleReproducible
// Default: if external exist, use it; else if vendor exist, use it; else scan source
// GogradleDevelop: external>vendor
// GogradleReproducible: vendor>external

public interface DependencyProduceStrategy {

    DependencyProduceStrategy DEFAULT_STRATEGY = new DefaultDependencyProduceStrategy();

    GolangDependencySet produce(ResolvedDependency dependency, File rootDir, DependencyVisitor visitor);
}
