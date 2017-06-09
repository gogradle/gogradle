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

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Manages global cache which users should never know.
 * Typically, global cache exists in ~/.gradle/go with following structure:
 * <br>+ ~/.gradle/go
 * <br>|
 * <br>|-- gopath   stores global go repository cache, e.g., golang.org/x/tools.
 * <br>|
 * <br>|-- binary   stores go distributions.
 * <br>|
 * <br>\-- metadata stores metadata of global go repository cache, e.g. repo url and last update time.
 * The metadata also acts as a lock in synchronization of multiple JVM process.
 */
public interface GlobalCacheManager {
    /**
     * Ensures global cache exists and is writable.
     */
    void ensureGlobalCacheExistAndWritable();

    /**
     * Get the go repository in global cache corresponding to {@code packagePath}
     *
     * @param packagePath import path of the package
     * @return the location of that repository in global cache
     */
    Path getGlobalPackageCachePath(String packagePath);

    /**
     * Get the path which locates in {@code relativePath} relative to ~/.gradle/go/binary
     *
     * @param relativePath the relative path to ~/.gradle/go/binary
     * @return the path
     */
    Path getGlobalGoBinCache(String relativePath);

    /**
     * Get the metadata of a package specified by {@code packagePath}
     *
     * @param packagePath import path of the package
     * @return the metadata if that package exists in global cache, {@code Optional.empty()} otherwise.
     */
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

    /**
     * Updates corresponding metadata (i.e. the lock) file of a dependency package.
     *
     * @param dependency the dependency package
     */
    void updateCurrentDependencyLock(GolangDependency dependency);

    /**
     * Check if the repository corresponding to a dependency package has been updated recently in global cache.
     *
     * @param dependency the dependency package
     * @return {@code true} if the package is up-to-date, {@code false} otherwise.
     * @see GolangPluginSetting#getGlobalCacheSecond()
     */
    boolean currentRepositoryIsUpToDate(NotationDependency dependency);
}
