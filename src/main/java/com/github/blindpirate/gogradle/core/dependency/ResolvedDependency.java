package com.github.blindpirate.gogradle.core.dependency;

import java.util.Map;

public interface ResolvedDependency extends GolangDependency {
    /**
     * The update time of a dependency. It will be used in resolving package conflict.
     * Generally speaking, package with newest update time will win.
     *
     * @return the update time determined by the package. It may be the last modified time
     * of a file on filesystem or in scm.
     */
    long getUpdateTime();

    /**
     * Get transitive dependencies of this package.
     *
     * @return the transitive dependencies
     */
    GolangDependencySet getDependencies();


    Map<String, String> toLockedNotation();

}
