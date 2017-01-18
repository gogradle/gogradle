package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public interface BuildManager {

    void prepareForBuild();

    void installDependency(ResolvedDependency dependency);

    void build();
}
