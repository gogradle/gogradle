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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.VcsNotationDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.function.Function

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class ProjectCacheManagerTest {
    @Mock
    PersistenceResolvedToDependenciesCache resolvedToDependenciesCache
    @Mock
    CloneBackedCache buildScopedNotationToResolvedCache
    @Mock
    CloneBackedCache buildScopedResolvedToDependenciesCache
    @Mock
    PersistenceNotationToResolvedCache persistenceNotationToResolvedCache
    @Mock
    VcsNotationDependency notationDependency
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    Function constructor = mock(Function)

    ProjectCacheManager projectCacheManager

    @Before
    void setUp() {
        projectCacheManager = new ProjectCacheManager(resolvedToDependenciesCache, persistenceNotationToResolvedCache)
        ReflectionUtils.setField(projectCacheManager, 'buildScopedNotationToResolvedCache', buildScopedNotationToResolvedCache)
        ReflectionUtils.setField(projectCacheManager, 'buildScopedResolvedToDependenciesCache', buildScopedResolvedToDependenciesCache)
    }

    @Test
    void 'loading cache should succeed'() {
        projectCacheManager.loadPersistenceCache()

        verify(persistenceNotationToResolvedCache).load()
        verify(resolvedToDependenciesCache).load()
    }

    @Test
    void 'saving cache should succeed'() {
        projectCacheManager.savePersistenceCache()

        verify(persistenceNotationToResolvedCache).save()
        verify(resolvedToDependenciesCache).save()
    }

    @Test
    void 'resolving should succeed'() {
        when(notationDependency.getCacheScope()).thenReturn(CacheScope.BUILD)
        projectCacheManager.resolve(notationDependency, constructor)
        verify(buildScopedNotationToResolvedCache).get(notationDependency, constructor)

        when(notationDependency.getCacheScope()).thenReturn(CacheScope.PERSISTENCE)
        projectCacheManager.resolve(notationDependency, constructor)
        verify(persistenceNotationToResolvedCache).get(notationDependency, constructor)
    }

    @Test
    void 'producing should succeed'() {
        when(resolvedDependency.getCacheScope()).thenReturn(CacheScope.BUILD)
        projectCacheManager.produce(resolvedDependency, constructor)
        verify(buildScopedResolvedToDependenciesCache).get(resolvedDependency, constructor)

        when(resolvedDependency.getCacheScope()).thenReturn(CacheScope.PERSISTENCE)
        projectCacheManager.produce(resolvedDependency, constructor)
        verify(resolvedToDependenciesCache).get(resolvedDependency, constructor)
    }
}
