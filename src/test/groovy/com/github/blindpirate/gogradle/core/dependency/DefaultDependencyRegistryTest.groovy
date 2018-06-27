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

package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.GolangRepository
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import javax.swing.text.html.Option

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultDependencyRegistryTest {
    @Mock
    NotationDependency notationDependency
    @Mock
    AbstractResolvedDependency resolvedDependency1
    @Mock
    AbstractResolvedDependency resolvedDependency2
    @Mock
    PackagePathResolver packagePathResolver

    DefaultDependencyRegistry registry

    @Before
    void setUp() {
        registry = new DefaultDependencyRegistry(packagePathResolver)
        when(resolvedDependency1.getName()).thenReturn("resolvedDependency")
        when(resolvedDependency2.getName()).thenReturn("resolvedDependency")
        when(resolvedDependency1.getDependencies()).thenReturn(GolangDependencySet.empty())
        when(resolvedDependency2.getDependencies()).thenReturn(GolangDependencySet.empty())
        when(resolvedDependency1.isFirstLevel()).thenReturn(false)
        when(resolvedDependency2.isFirstLevel()).thenReturn(false)
        when(packagePathResolver.produce(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                String path = invocation.arguments[0]
                return Optional.of(VcsGolangPackage
                        .builder()
                        .withPath(path)
                        .withRootPath(path)
                        .withRepository(GolangRepository.newOriginalRepository(VcsType.GIT, 'url'))
                        .build())
            }
        })
    }

    @Test
    void 'dependency should be put at the first time'() {
        assert registry.register(resolvedDependency1)
    }

    @Test
    void "a dependency's root should not be put"() {
        when(packagePathResolver.produce(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return Optional.of(VcsGolangPackage
                        .builder()
                        .withPath('resolvedDependency')
                        .withRootPath('resolvedDependency')
                        .withRepository(GolangRepository.newOriginalRepository(VcsType.GIT, 'url'))
                        .build())
            }
        })
        when(resolvedDependency2.getName()).thenReturn('resolvedDependency/sub')

        registry.register(resolvedDependency1)
        registry.register(resolvedDependency2)
        assert registry.packages.size() == 1
    }

    @Test
    void 'newer dependency should be put'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(1L)
        when(resolvedDependency2.getUpdateTime()).thenReturn(2L)
        // when
        registry.register(resolvedDependency1)
        // then
        assert registry.register(resolvedDependency2)
    }

    @Test
    void 'older dependency should not be put'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(2L)
        when(resolvedDependency2.getUpdateTime()).thenReturn(1L)
        // when
        registry.register(resolvedDependency1)
        // then
        assert !registry.register(resolvedDependency2)
    }

    @Test
    void 'first level dependency should always win'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(2L)
        when(resolvedDependency2.getUpdateTime()).thenReturn(1L)
        // resolvedDependency2 is old but first level
        when(resolvedDependency2.isFirstLevel()).thenReturn(true)
        // when
        registry.register(resolvedDependency1)
        // then
        assert registry.register(resolvedDependency2)
        assert !registry.register(resolvedDependency1)
    }

    @Test
    void 'same dependency should not be put'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(1L)
        // when
        registry.register(resolvedDependency1)
        // then
        assert !registry.register(resolvedDependency1)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if first-level dependencies conflict'() {
        // given
        when(resolvedDependency1.isFirstLevel()).thenReturn(true)
        when(resolvedDependency2.isFirstLevel()).thenReturn(true)
        // then
        registry.register(resolvedDependency1)
        registry.register(resolvedDependency2)
    }

    @Test
    void 'retrieving should succeed'() {
        // when
        registry.register(resolvedDependency1)
        // then
        assert registry.retrieve('resolvedDependency').get().is(resolvedDependency1)
    }

    // https://github.com/gogradle/gogradle/issues/207
    @Test
    void "replaced dependency's descendants should be removed"() {
        // given
        AbstractResolvedDependency root = MockUtils.mockResolvedDependency('root')
        AbstractResolvedDependency b = MockUtils.mockResolvedDependency('b')
        AbstractResolvedDependency e1 = MockUtils.mockResolvedDependency('e', 1L)
        AbstractResolvedDependency e2 = MockUtils.mockResolvedDependency('e', 2L)
        AbstractResolvedDependency f1 = MockUtils.mockResolvedDependency('f', 1L)
        AbstractResolvedDependency f2 = MockUtils.mockResolvedDependency('f', 2L)

        bindDependencies(root, b, e1)
        bindDependencies(b, e2)
        bindDependencies(e1, f2)
        bindDependencies(e2, f1)
        bindDependencies(f1, [] as ResolvedDependency[])
        bindDependencies(f2, [] as ResolvedDependency[])

        // when
        assert registry.register(root)
        assert registry.register(e1)
        assert registry.register(b)
        assert registry.register(f2)
        assert registry.register(e2)
        assert registry.register(f1)

        // then
        assert registry.retrieve('root').get().is(root)
        assert registry.retrieve('b').get().is(b)
        assert registry.retrieve('e').get().is(e2)
        assert registry.retrieve('f').get().is(f1)
    }

    private void bindDependencies(ResolvedDependency parent, ResolvedDependency... children) {
        GolangDependencySet set = new GolangDependencySet(children as List)
        when(parent.getDependencies()).thenReturn(set)
    }
}
