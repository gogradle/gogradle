package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import org.junit.Test

import java.util.function.Function

import static org.mockito.ArgumentMatchers.isNull
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class ProjectCacheManagerTest {
    BuildScopedNotationCache buildScopedNotationCache = mock(BuildScopedNotationCache)
    ConcreteNotationToResolvedDependencyCache concreteNotationToResolvedDependencyCache = mock(ConcreteNotationToResolvedDependencyCache)
    ResolveDependencyToDependenciesCache resolveDependencyToDependenciesCache = mock(ResolveDependencyToDependenciesCache)

    ProjectCacheManager projectCacheManager = new ProjectCacheManager(buildScopedNotationCache,
            concreteNotationToResolvedDependencyCache,
            resolveDependencyToDependenciesCache)

    NotationDependency notationDependency = mock(NotationDependency)
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
