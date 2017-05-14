/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

// by default the global cache directory is located in ~/.gradle/go/
// + ~/.gradle/go
//   - gopath
//   - binary
//   - metadata
public interface GlobalCacheManager {
    void ensureGlobalCacheExistAndWritable();

    Path getGlobalPackageCachePath(String packagePath);

    Path getGlobalGoBinCache(String relativePath);

    Optional<GlobalCacheMetadata> getMetadata(Path packagePath);

    /**
     * Locks global cache directory of {@code dependency}, and call the {@code callable}
     *
     * @param dependency dependency to be locked
     * @param callable   code to be executed under lock
     * @param <T>        return value type of callable
     * @return the return value of callable
     * @throws Exception exception thrown by callable
     */
    <T> T runWithGlobalCacheLock(GolangDependency dependency, Callable<T> callable) throws Exception;

    void updateCurrentDependencyLock(GolangDependency dependency);

    boolean currentRepositoryIsUpToDate(NotationDependency dependency);
}
