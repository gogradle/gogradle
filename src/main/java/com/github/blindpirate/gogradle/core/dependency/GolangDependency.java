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

/**
 * A {@link GolangDependency} represents a dependency
 * such as a specific version of source code or a local directory.
 */
public interface GolangDependency extends Dependency, Serializable, GolangCloneable {
    /**
     * The dependency's import path, e.g., golang.org/x/crypto/cmd.
     * <p>
     * However, currently we support golang.org/x/crypto (the root path) only.
     *
     * @return dependency's import path
     */
    @Override
    String getName();

    /**
     * A unique identifier to locate a dependency, e.g., git commit id.
     *
     * @return the version string
     */
    @Override
    String getVersion();

    GolangPackage getPackage();

    ResolvedDependency resolve(ResolveContext context);

    boolean isFirstLevel();

    CacheScope getCacheScope();
}

