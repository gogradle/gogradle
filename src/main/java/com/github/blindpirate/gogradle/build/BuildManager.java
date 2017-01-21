package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public interface BuildManager {

    void prepareSymbolicLinks();

    void installDependency(ResolvedDependency dependency, Configuration configuration);

    void build();

    void test();
}
