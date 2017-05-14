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

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class ProjectCacheManager {
    private final BuildScopedNotationToResolvedCache buildScopedNotationToResolvedCache;
    private final BuildScopedResolvedToDependenciesCache buildScopedResolvedToDependenciesCache;
    private final PersistenceNotationToResolvedCache persistenceNotationToResolvedCache;
    private final PersistenceResolvedToDependenciesCache persistenceResolvedToDependenciesCache;

    @Inject
    public ProjectCacheManager(BuildScopedNotationToResolvedCache buildScopedNotationToResolvedCache,
                               BuildScopedResolvedToDependenciesCache buildScopedResolvedToDependenciesCache,
                               PersistenceNotationToResolvedCache persistenceNotationToResolvedCache,
                               PersistenceResolvedToDependenciesCache persistenceResolvedToDependenciesCache) {
        this.buildScopedNotationToResolvedCache = buildScopedNotationToResolvedCache;
        this.buildScopedResolvedToDependenciesCache = buildScopedResolvedToDependenciesCache;
        this.persistenceNotationToResolvedCache = persistenceNotationToResolvedCache;
        this.persistenceResolvedToDependenciesCache = persistenceResolvedToDependenciesCache;
    }

    public void loadPersistenceCache() {
        persistenceNotationToResolvedCache.load();
        persistenceResolvedToDependenciesCache.load();
    }

    public void savePersistenceCache() {
        persistenceNotationToResolvedCache.save();
        persistenceResolvedToDependenciesCache.save();
    }

    public ResolvedDependency resolve(NotationDependency notationDependency,
                                      Function<NotationDependency, ResolvedDependency> constructor) {
        if (notationDependency.getCacheScope() == CacheScope.BUILD) {
            return buildScopedNotationToResolvedCache.get(notationDependency, constructor);
        } else {
            return persistenceNotationToResolvedCache.get(notationDependency, constructor);
        }
    }

    public GolangDependencySet produce(ResolvedDependency resolvedDependency,
                                       Function<ResolvedDependency, GolangDependencySet> constructor) {
        if (resolvedDependency.getCacheScope() == CacheScope.BUILD) {
            return buildScopedResolvedToDependenciesCache.get(resolvedDependency, constructor);
        } else {
            return persistenceResolvedToDependenciesCache.get(resolvedDependency, constructor);
        }
    }
}
