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
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultDependencyRegistryTest {
    @Mock
    NotationDependency notationDependency
    @Mock
    AbstractResolvedDependency resolvedDependency1
    @Mock
    AbstractResolvedDependency resolvedDependency2

    DefaultDependencyRegistry registry = new DefaultDependencyRegistry()

    @Before
    void setUp() {
        when(resolvedDependency1.getName()).thenReturn("resolvedDependency")
        when(resolvedDependency2.getName()).thenReturn("resolvedDependency")
        when(resolvedDependency1.isFirstLevel()).thenReturn(false)
        when(resolvedDependency2.isFirstLevel()).thenReturn(false)
    }

    @Test
    void 'dependency should be put at the first time'() {
        assert registry.register(resolvedDependency1)
    }

    @Test(expected = IllegalStateException)
    void 'a dependency should not be put if its prefix exists'() {
        registry.register(resolvedDependency1)
        when(resolvedDependency2.getName()).thenReturn('resolvedDependency/sub')
        registry.register(resolvedDependency2)
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
    void 'retriving should succeed'() {
        // when
        registry.register(resolvedDependency1)
        // then
        assert registry.retrieve('resolvedDependency').is(resolvedDependency1)
    }

}
