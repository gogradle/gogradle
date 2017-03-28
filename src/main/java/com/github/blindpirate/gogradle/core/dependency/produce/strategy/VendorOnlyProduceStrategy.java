package com.github.blindpirate.gogradle.core.dependency.produce.strategy;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.util.logging.DebugLog;

import javax.inject.Singleton;
import java.io.File;

@Singleton
public class VendorOnlyProduceStrategy extends ExclusionInheritanceProduceStrategry {
    @Override
    @DebugLog
    public GolangDependencySet doProduce(ResolvedDependency dependency,
                                         File rootDir,
                                         DependencyVisitor visitor,
                                         String configuration) {
        return visitor.visitVendorDependencies(dependency, rootDir);
    }
}

