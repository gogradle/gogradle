package com.github.blindpirate.gogradle.core.dependency.produce.strategy;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.util.logging.DebugLog;

import javax.inject.Singleton;
import java.io.File;

/**
 * Default strategy to generate dependencies of a package.
 * <p>
 * First, it will check if there are external package manager and vendor director.
 * If so, use them and let vendor dependencies have higher priority.
 * Otherwise, as a fallback, it will scan source code to get dependencies.
 */
@Singleton
public class DefaultDependencyProduceStrategy implements DependencyProduceStrategy {
    @Override
    @DebugLog
    public GolangDependencySet produce(ResolvedDependency dependency, File rootDir, DependencyVisitor vistor) {
        GolangDependencySet externalDependencies = vistor.visitExternalDependencies(dependency, rootDir);

        GolangDependencySet vendorDependencies = vistor.visitVendorDependencies(dependency, rootDir);

        GolangDependencySet candicate = GolangDependencySet.merge(vendorDependencies, externalDependencies);

        if (candicate.isEmpty()) {
            return vistor.visitSourceCodeDependencies(dependency, rootDir);
        } else {
            return candicate;
        }

    }
}
