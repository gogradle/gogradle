package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency
import org.junit.Test

import java.util.function.Function

import static org.mockito.Mockito.*

class ProjectCacheManagerTest {
    BuildScopedVcsNotationCache buildScopedNotationCache = mock(BuildScopedVcsNotationCache)
    ConcreteVcsNotationToResolvedDependencyCache concreteNotationToResolvedDependencyCache = mock(ConcreteVcsNotationToResolvedDependencyCache)
    ResolveDependencyToDependenciesCache resolveDependencyToDependenciesCache = mock(ResolveDependencyToDependenciesCache)

    ProjectCacheManager projectCacheManager = new ProjectCacheManager(buildScopedNotationCache,
            concreteNotationToResolvedDependencyCache,
            resolveDependencyToDependenciesCache)

    GitMercurialNotationDependency notationDependency = mock(GitMercurialNotationDependency)
    ResolvedDependency resolvedDependency = mock(ResolvedDependency)
    Function constructor = mock(Function)

    @Test
    void 'loading cache should succeed'() {
        projectCacheManager.loadPersistenceCache()

        verify(concreteNotationToResolvedDependencyCache).load()
        verify(resolveDependencyToDependenciesCache).load()
    }

    @Test
    void 'saving cache should succeed'() {
        projectCacheManager.savePersistenceCache()

        verify(concreteNotationToResolvedDependencyCache).save()
        verify(resolveDependencyToDependenciesCache).save()
    }

    @Test
    void 'resolving should succeed'() {
        projectCacheManager.resolve(notationDependency, constructor)
        verify(buildScopedNotationCache).get(notationDependency, constructor)

        when(notationDependency.isConcrete()).thenReturn(true)
        projectCacheManager.resolve(notationDependency, constructor)
        verify(concreteNotationToResolvedDependencyCache).get(notationDependency, constructor)
    }

    @Test
    void 'producing should succeed'() {
        projectCacheManager.produce(resolvedDependency, constructor)
        verify(resolveDependencyToDependenciesCache).get(resolvedDependency, constructor)
    }
}
