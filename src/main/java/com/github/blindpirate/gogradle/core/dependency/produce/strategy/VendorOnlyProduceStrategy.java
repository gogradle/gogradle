package com.github.blindpirate.gogradle.core.dependency.produce.strategy;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.util.logging.DebugLog;

import javax.inject.Singleton;
import java.io.File;
import java.util.Optional;

@Singleton
public class VendorOnlyProduceStrategy implements DependencyProduceStrategy {
    @Override
    @DebugLog
    public GolangDependencySet produce(ResolvedDependency dependency, File rootDir, DependencyVisitor visitor) {
        return visitor.visitVendorDependencies(dependency, rootDir);
    }
}

