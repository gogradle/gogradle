package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

import java.nio.file.Path;
import java.util.concurrent.Callable;

// by default the global cache directory is located in ~/.gradle/go/
// + ~/.gradle/go
//   - gopath
//   - binary
// the project-specific directory is located in ${projectRoot}/build/go
// + ${projectRoot}
//   - build
//     - gopath
public interface CacheManager {
    void ensureGlobalCacheExistAndWritable();

    void ensureProjectBuildCacheExist();

    Path getGlobalCachePath(String packageName);

    /**
     * Locks global cache directory of {@code dependency}, and call the {@code callable}
     *
     * @param dependency
     * @param callable
     * @param <T>
     * @return
     */
    <T> T runWithGlobalCacheLock(GolangDependency dependency, Callable<T> callable);

}
