package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.resolve.ModuleDependencyVistor;

/**
 * Direct how to generate dependencies of an existing golang package module.
 */
// ExternalOnly/VendorOnly/SourceCodeOnly/GogradleDevelop/GogradleReproducible
// Default: if external exist, use it; else if vendor exist, use it; else scan source
// GogradleDevelop: external>vendor
// GogradleReproducible: vendor>external

public interface DependencyProduceStrategy {

    DependencyProduceStrategy DEFAULT_STRATEGY = new DefaultDependencyProduceStrategy();

    GolangDependencySet produce(GolangPackageModule module, ModuleDependencyVistor vistor);
}
