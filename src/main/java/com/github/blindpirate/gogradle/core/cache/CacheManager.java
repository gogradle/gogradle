package com.github.blindpirate.gogradle.core.cache;

import java.nio.file.Path;

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
}
