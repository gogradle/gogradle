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

package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency
import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.junit.Test
import org.mockito.Mockito

import static org.mockito.Mockito.*

class GolangConfigurationTest {
    NotationParser parser = mock(NotationParser)
    GolangConfiguration configuration = new GolangConfiguration('build', parser, null)

    @Test
    void 'dependency registry should be isolated'() {
        GolangConfiguration build = new GolangConfiguration('build', parser, null)
        GolangConfiguration test = new GolangConfiguration('test', parser, null)
        assert build.dependencyRegistry instanceof DefaultDependencyRegistry
        assert !build.dependencyRegistry.is(test.dependencyRegistry)
    }

    @Test
    void 'getting dependencies should succeed'() {
        assert configuration.getDependencies().isEmpty()
    }

    @Test
    void 'adding first level dependency should succeed'() {
        // when
        configuration.addFirstLevelDependency([:], { 1 })
        // then
        List firstLevelDependencies = configuration.firstLevelDependencies
        assert firstLevelDependencies.size() == 1
        assert firstLevelDependencies[0].left == [:]
        assert firstLevelDependencies[0].right() == 1
        assert configuration.hasFirstLevelDependencies()
    }

    @Test
    void 'resolving first level dependencies should succeed'() {
        // given
        'adding first level dependency should succeed'()
        GolangDependency dependency = DependencyUtils.mockWithName(AbstractGolangDependency, '')
        when(parser.parse(Mockito.any())).thenReturn(dependency)
        // when
        configuration.resolveFirstLevelDependencies()
        // then
        assert configuration.dependencies.size() == 1
        verify(parser).parse([:])
    }
}
