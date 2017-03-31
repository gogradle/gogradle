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
public class DefaultDependencyProduceStrategy extends ExclusionInheritanceProduceStrategy {
    @Override
    @DebugLog
    public GolangDependencySet doProduce(ResolvedDependency dependency,
                                         File rootDir,
                                         DependencyVisitor visitor,
                                         String configuration) {
        GolangDependencySet candidate = visitor.visitExternalDependencies(dependency,
                rootDir, configuration);

        if (!candidate.isEmpty()) {
            return candidate;
        }

        candidate = visitor.visitVendorDependencies(dependency, rootDir);

        if (!candidate.isEmpty()) {
            return candidate;
        }

        return visitor.visitSourceCodeDependencies(dependency, rootDir, configuration);
    }
}
