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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultDependencyVisitorTest {
    @Mock
    ExternalDependencyFactory external1
    @Mock
    ExternalDependencyFactory external2
    @Mock
    SourceCodeDependencyFactory sourceCodeDependencyFactory
    @Mock
    VendorDependencyFactory vendorDependencyFactory
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    GolangDependencySet dependencySet
    @Mock
    DependencyProduceStrategy strategy
    @Mock
    File rootDir
    File resource

    DefaultDependencyVisitor visitor

    @Before
    void setUp() {
        visitor = new DefaultDependencyVisitor(
                [external1, external2],
                sourceCodeDependencyFactory,
                vendorDependencyFactory
        )
    }

    @Test
    void 'visiting external dependencies should succeed'() {
        // given:
        when(external1.canRecognize(rootDir)).thenReturn(false)
        when(external2.canRecognize(rootDir)).thenReturn(true)
        when(external2.produce(rootDir, 'build')).thenReturn(dependencySet)

        // then:
        assert visitor.visitExternalDependencies(resolvedDependency, rootDir, 'build') == dependencySet
    }

    @Test
    void 'visiting source dependencies should succeed'() {
        // given:
        when(sourceCodeDependencyFactory.produce(resolvedDependency, rootDir, 'build')).thenReturn(dependencySet)
        // then:
        assert visitor.visitSourceCodeDependencies(resolvedDependency, rootDir, 'build') == dependencySet
    }

    @Test
    void 'visiting vendor dependencies should succeed'() {
        // given:
        when(vendorDependencyFactory.produce(resolvedDependency, rootDir)).thenReturn(GolangDependencySet.empty())
        // then:
        assert visitor.visitVendorDependencies(resolvedDependency, rootDir, 'build').isEmpty()

        //given:
        when(vendorDependencyFactory.produce(resolvedDependency, rootDir)).thenReturn(dependencySet)
        // then:
        assert visitor.visitVendorDependencies(resolvedDependency, rootDir, 'build') == dependencySet
    }

    @Test
    @WithResource('')
    void 'empty set should be returned when no external dependencies exist'() {
        assert visitor.visitExternalDependencies(resolvedDependency, resource, 'build').isEmpty()
    }

}
