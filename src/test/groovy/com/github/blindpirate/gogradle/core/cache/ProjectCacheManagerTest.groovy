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

package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency
import org.junit.Test

import java.util.function.Function

import static org.mockito.Mockito.*

class ProjectCacheManagerTest {
    BuildScopedNotationToResolvedCache buildScopedNotationCache = mock(BuildScopedNotationToResolvedCache)
    PersistenceNotationToResolvedCache persistenceNotationCache = mock(PersistenceNotationToResolvedCache)
    BuildScopedResolvedToDependenciesCache buildScopedResolvedCache = mock(BuildScopedResolvedToDependenciesCache)
    PersistenceResolvedToDependenciesCache persistenceResolvedCache = mock(PersistenceResolvedToDependenciesCache)

    ProjectCacheManager projectCacheManager = new ProjectCacheManager(
            buildScopedNotationCache,
            buildScopedResolvedCache,
            persistenceNotationCache,
            persistenceResolvedCache)

    GitMercurialNotationDependency notationDependency = mock(GitMercurialNotationDependency)
    ResolvedDependency resolvedDependency = mock(ResolvedDependency)
    Function constructor = mock(Function)

    @Test
    void 'loading cache should succeed'() {
        projectCacheManager.loadPersistenceCache()

        verify(persistenceNotationCache).load()
        verify(persistenceResolvedCache).load()
    }

    @Test
    void 'saving cache should succeed'() {
        projectCacheManager.savePersistenceCache()

        verify(persistenceNotationCache).save()
        verify(persistenceResolvedCache).save()
    }

    @Test
    void 'resolving should succeed'() {
        when(notationDependency.getCacheScope()).thenReturn(CacheScope.BUILD)
        projectCacheManager.resolve(notationDependency, constructor)
        verify(buildScopedNotationCache).get(notationDependency, constructor)

        when(notationDependency.getCacheScope()).thenReturn(CacheScope.PERSISTENCE)
        projectCacheManager.resolve(notationDependency, constructor)
        verify(persistenceNotationCache).get(notationDependency, constructor)
    }

    @Test
    void 'producing should succeed'() {
        when(resolvedDependency.getCacheScope()).thenReturn(CacheScope.BUILD)
        projectCacheManager.produce(resolvedDependency, constructor)
        verify(buildScopedResolvedCache).get(resolvedDependency, constructor)

        when(resolvedDependency.getCacheScope()).thenReturn(CacheScope.PERSISTENCE)
        projectCacheManager.produce(resolvedDependency, constructor)
        verify(persistenceResolvedCache).get(resolvedDependency, constructor)
    }
}
