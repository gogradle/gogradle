package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;

import javax.inject.Singleton;

/**
 * Scans all code to generate dependencies.
 */
@Singleton
public class SourceCodeDependencyFactory implements DependencyFactory {
    @Override
    public GolangDependencySet produce(GolangPackageModule module) {
        throw new IllegalStateException("Not implemented yet!");
    }

    @Override
    public boolean accept(GolangPackageModule module) {
        throw new IllegalStateException("Not implemented yet!");
    }
}
