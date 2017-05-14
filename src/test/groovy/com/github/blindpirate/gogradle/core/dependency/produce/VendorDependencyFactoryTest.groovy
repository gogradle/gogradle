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

package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class VendorDependencyFactoryTest {
    File resource

    @InjectMocks
    VendorDependencyFactory factory

    @Mock
    PackagePathResolver resolver

    @Mock
    DependencyVisitor visitor

    @Mock
    VcsAccessor accessor

    @Mock
    VcsResolvedDependency resolvedDependency

    ProjectCacheManager projectCacheManager = MockUtils.projectCacheManagerWithoutCache()

    GolangPackage golangPackage = MockUtils.mockVcsPackage()

    @Before
    void setUp() {
        when(GogradleGlobal.INSTANCE.getInstance(ProjectCacheManager)).thenReturn(projectCacheManager)
        when(resolvedDependency.getVcsType()).thenReturn(VcsType.GIT)
    }

    @Test
    void 'directory without vendor should produce an empty dependency set'() {
        assert factory.produce(resolvedDependency, resource).isEmpty()
    }

    @Test
    void 'producing a vendor dependency should succeed'() {
        // given
        when(resolver.produce('root')).thenReturn(of(IncompleteGolangPackage.of('root')))
        when(resolver.produce('root/package')).thenReturn(of(golangPackage))
        when(GogradleGlobal.INSTANCE.getInstance(DependencyVisitor)).thenReturn(visitor)
        when(GogradleGlobal.INSTANCE.getInjector().getInstance((Key) any(Key))).thenReturn(accessor)
        when(visitor.visitVendorDependencies(any(ResolvedDependency), any(File), anyString()))
                .thenReturn(GolangDependencySet.empty())
        IOUtils.write(resource, 'vendor/root/package/main.go', '')

        // when
        GolangDependencySet set = factory.produce(resolvedDependency, resource)
        // then
        GolangDependency dependency = set.first()
        assert dependency instanceof VendorResolvedDependency
        assert ReflectionUtils.getField(dependency, 'hostDependency').is(resolvedDependency)
        assert ReflectionUtils.getField(dependency, 'relativePathToHost') == 'vendor/root/package'
    }

}
