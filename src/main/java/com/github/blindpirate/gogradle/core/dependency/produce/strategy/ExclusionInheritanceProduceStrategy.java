package com.github.blindpirate.gogradle.core.dependency.produce.strategy;

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;

import java.io.File;

public abstract class ExclusionInheritanceProduceStrategy implements DependencyProduceStrategy {
    @Override
    public GolangDependencySet produce(ResolvedDependency dependency,
                                       File rootDir,
                                       DependencyVisitor visitor,
                                       String configuration) {
        GolangDependencySet ret = doProduce(dependency, rootDir, visitor, configuration);
        ret.stream()
                .map(d -> (AbstractGolangDependency) d)
                .forEach(d -> d.inheritExclusions(dependency));
        return ret;
    }

    protected abstract GolangDependencySet doProduce(ResolvedDependency dependency,
                                                     File rootDir,
                                                     DependencyVisitor visitor,
                                                     String configuration);
}
