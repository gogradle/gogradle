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
