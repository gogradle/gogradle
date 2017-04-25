package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.logging.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.function.Function

import static com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManagerTest.APPLY_FUNCTION_ANSWER
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when


@RunWith(GogradleRunner)
class CacheEnabledDependencyResolverMixinTest {
    @Mock
    CacheEnabledDependencyResolverMixin delegate
    @Mock
    ProjectCacheManager projectCacheManager
    @Mock
    Logger logger
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    ResolveContext context

    NotationDependency notationDependency = DependencyUtils.mockWithName(NotationDependency, 'notation')

    @Before
    void setUp() {
        ReflectionUtils.setStaticFinalField(CacheEnabledDependencyResolverMixin,'LOGGER',logger)
    }

    @Test
    void 'cached result should be logged'() {
        // given
        when(projectCacheManager.resolve(any(NotationDependency), any(Function)))
                .thenReturn(resolvedDependency)
        // then
        assert new CacheEnabledDependencyResolverMixinForTest().resolve(context, notationDependency).is(resolvedDependency)
        verify(logger).quiet('Resolving cached {}', notationDependency)
    }

    @Test
    void 'non-cached result should be logged'() {
        // given
        when(projectCacheManager.resolve(any(NotationDependency), any(Function)))
                .thenAnswer(APPLY_FUNCTION_ANSWER)
        when(delegate.doResolve(context, notationDependency)).thenReturn(resolvedDependency)
        // then
        assert new CacheEnabledDependencyResolverMixinForTest().resolve(context, notationDependency).is(resolvedDependency)
        verify(logger).quiet('Resolving {}', notationDependency)
    }

    class CacheEnabledDependencyResolverMixinForTest implements CacheEnabledDependencyResolverMixin {
        @Override
        void install(ResolvedDependency dependency, File targetDirectory) {
        }

        @Override
        ProjectCacheManager getProjectCacheManager() {
            return CacheEnabledDependencyResolverMixinTest.this.projectCacheManager
        }

        @Override
        ResolvedDependency doResolve(ResolveContext context, NotationDependency dependency) {
            return delegate.doResolve(context, dependency)
        }
    }
}
