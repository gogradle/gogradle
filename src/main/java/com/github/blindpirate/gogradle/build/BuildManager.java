package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.util.List;

public interface BuildManager {

    void ensureDotVendorDirNotExist();

    void prepareSymbolicLinks();

    void installDependency(ResolvedDependency dependency, Configuration configuration);

    void installDependencyToVendor(ResolvedDependency dependency);

    void build();

    void test();

    void testWithPatterns(List<String> testNamePattern);
}
