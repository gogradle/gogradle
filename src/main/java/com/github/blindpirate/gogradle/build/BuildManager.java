package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

public interface BuildManager {
    String GOGRADLE_BUILD_DIR = ".gogradle";
    String BUILD_GOPATH = "build_gopath";

    void installDependency(ResolvedDependency dependency);

    void build();
}
