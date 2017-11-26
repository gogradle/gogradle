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

package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangCloneable;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.cache.CacheScope;
import org.gradle.api.artifacts.Dependency;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents a dependency package, for example, such as a specific version of golang/x/crypto
 * or a local directory. Typically, the minimum dependency unit is repository root directory.
 */
public interface GolangDependency extends Dependency, Serializable, GolangCloneable {
    /**
     * The dependency's import path, e.g., golang.org/x/crypto/cmd.
     * However, currently we support golang.org/x/crypto (the root path) only.
     *
     * @return dependency's import path
     */
    @Override
    String getName();

    /**
     * An unique identifier to locate a dependency, e.g., git commit id.
     *
     * @return the version string
     */
    @Override
    String getVersion();

    /**
     * Get the package this dependency stands for.
     *
     * @return the package
     * @see GolangPackage
     */
    GolangPackage getPackage();

    /**
     * Resolve to a concrete dependency which can be located to a specific version of code.
     *
     * @param context the resolve context
     * @return resolved dependency
     */
    ResolvedDependency resolve(ResolveContext context);

    /**
     * A dependency is said to be "first-level" when it is defined in build.gradle or gogradle.lock of root project.
     *
     * @return <code>true</code> if it is "first-level", <code>false</code> otherwise.
     */
    boolean isFirstLevel();

    /**
     * Get cache scope of this dependency.
     *
     * @return the cache scope
     */
    CacheScope getCacheScope();

    /**
     * This dependencies sub packages. A sub package is a string representing the relative path to the repo root.
     * This concept is inspired by <a href="https://github.com/Masterminds/glide">glide</a>
     *
     * @return the set of sub packages
     */
    Set<String> getSubpackages();

    /**
     * All descendants files and directories.
     */
    String ALL_DESCENDANTS = "...";
    /**
     * Only files located in repo root.
     */
    String ONLY_CURRENT_FILES = ".";
}

