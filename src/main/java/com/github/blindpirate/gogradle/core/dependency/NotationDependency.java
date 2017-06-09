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

import java.util.Set;
import java.util.function.Predicate;

/**
 * Represents a dependency package defined by some notations, e.g.
 * {@code [name:'golang/x/tools', commitId:'1a2b3c4d5e', transitive: false]}.
 * A NotationDependency may not be concrete since it could contains an specific version,
 * e.g. {@code [name:'golang.org/x/tools, version:'LATEST_VERSION']}
 */
public interface NotationDependency extends GolangDependency {
    /**
     * The set used to exclude some dependency packages.
     *
     * @return the set of predicates
     */
    Set<Predicate<GolangDependency>> getTransitiveDepExclusions();
}
