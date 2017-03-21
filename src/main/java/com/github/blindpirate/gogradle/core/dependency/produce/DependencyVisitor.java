package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;

public interface DependencyVisitor {

    /**
     * Visits dependencies managed by a external package management tool such as godep, govendor, etc.
     * Usually, it's determined by analyzing "lock file" of that tool.
     *
     * @param dependency    The dependency to visit
     * @param rootDir       Root directory of a package
     * @param configuration Current context, {@code GolangConfiguration.BUILD} or {@code GolangConfiguration.TEST}
     * @return Dependencies managed by external package management tools
     */
    GolangDependencySet visitExternalDependencies(ResolvedDependency dependency,
                                                  File rootDir,
                                                  String configuration);

    /**
     * Visits dependencies in vendor.
     *
     * @param dependency The dependency to visit
     * @param rootDir    Root directory of a package
     * @return Dependencies in vendor directory
     */
    GolangDependencySet visitVendorDependencies(ResolvedDependency dependency, File rootDir);

    /**
     * Analyze the imports in source code (all .go files in root directory except vendor) to get dependencies.
     *
     * @param dependency    The dependency to visit
     * @param rootDir       Root directory of a package
     * @param configuration Current context, build or test
     * @return All imported package.
     */
    GolangDependencySet visitSourceCodeDependencies(ResolvedDependency dependency,
                                                    File rootDir,
                                                    String configuration);
}
