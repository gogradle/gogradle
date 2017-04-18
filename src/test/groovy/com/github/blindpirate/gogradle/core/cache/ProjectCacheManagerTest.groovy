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
