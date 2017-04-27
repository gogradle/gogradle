package com.github.blindpirate.gogradle.core.dependency.produce.strategy;

import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency;
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
    public GolangDependencySet produce(ResolvedDependency dependency,
                                       File rootDir,
                                       DependencyVisitor visitor,
                                       String configuration) {
        GolangDependencySet externalDependencies = visitor.visitExternalDependencies(dependency,
                rootDir, configuration);

        GolangDependencySet vendorDependencies = visitor.visitVendorDependencies(dependency, rootDir, configuration);

        GolangDependencySet candidate = GolangDependencySet.merge(vendorDependencies, externalDependencies);

        if (candidate.isEmpty()) {
            candidate = visitor.visitSourceCodeDependencies(dependency, rootDir, configuration);
        }

        return candidate.stream()
                .filter(d -> !isGogradleVendorDependency(d))
                .collect(GolangDependencySet.COLLECTOR);
    }

    private boolean isGogradleVendorDependency(GolangDependency dependency) {
        if (dependency instanceof VendorNotationDependency) {
            VendorNotationDependency vendorNotationDependency = (VendorNotationDependency) dependency;
            return vendorNotationDependency.getHostNotationDependency() instanceof GogradleRootProject;
        } else {
            return false;
        }
    }
}
