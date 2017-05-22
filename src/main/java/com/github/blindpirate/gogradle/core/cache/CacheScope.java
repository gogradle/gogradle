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

import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;

/**
 * Represents a dependency's cache scope. When a dependency whose code for is not fixed,
 * say, a dependency existing on local file system, or a git dependency declared with commit "LATEST_COMMIT",
 * it has a @{code BUILD} scope. Otherwise, it has a @{code PERSISTENCE} scope, e.g., a git dependency with
 * concrete commit.
 *
 * @see LocalDirectoryDependency#getCacheScope()
 * @see GitMercurialNotationDependency#getCacheScope()
 */
public enum CacheScope {
    BUILD,
    PERSISTENCE
}
