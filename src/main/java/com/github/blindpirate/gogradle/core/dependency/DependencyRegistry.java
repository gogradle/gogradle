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

import java.util.Optional;

/**
 * Manages global dependency register.
 * When a dependency is resolved, its repo information will be registered here.
 * Later, resolving dependency of same repo can be faster.
 */
public interface DependencyRegistry {
    /**
     * Registers a dependency. In this course, conflicts will be resolved or thrown.
     *
     * @param dependency The dependency to register
     * @return <code>true</code> if the dependency is newer than existing dependency with same name,
     * <code>false</code> otherwise.
     */
    boolean register(ResolvedDependency dependency);

    /**
     * Retrieve a registered dependency from the registry.
     *
     * @param name the name of dependency to be retrieved
     * @return the corresponding dependency, @{code null} if it does not exist in this registry
     */
    Optional<ResolvedDependency> retrieve(String name);
}
