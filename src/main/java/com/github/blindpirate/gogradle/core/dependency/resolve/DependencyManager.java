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

package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.io.File;

public interface DependencyManager {



    /**
     * Resolves a dependency.
     * During this process, right version will be determined by VCS.
     *
     * @param context    the configuration this dependency in and current exclusion specs
     * @param dependency dependency to be resolved
     * @return the resolved dependency
     */
    ResolvedDependency resolve(ResolveContext context, NotationDependency dependency);


    /**
     * Copy all necessary files of this {@code dependency} to the {@code targetDirectory}.
     *
     * @param dependency      the dependency
     * @param targetDirectory the target directory
     */
    void install(ResolvedDependency dependency, File targetDirectory);
}
