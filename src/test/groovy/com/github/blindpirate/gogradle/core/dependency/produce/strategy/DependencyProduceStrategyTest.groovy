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

package com.github.blindpirate.gogradle.core.dependency.produce.strategy

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import org.junit.Before
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static org.mockito.Mockito.when

abstract class DependencyProduceStrategyTest {
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    File rootDir
    @Mock
    DependencyVisitor visitor

    @Mock
    AbstractGolangDependency a1
    @Mock
    AbstractGolangDependency b1
    @Mock
    AbstractGolangDependency c1
    @Mock
    AbstractGolangDependency a2
    @Mock
    AbstractResolvedDependency b2
    @Mock
    AbstractGolangDependency c2

    @Before
    void superSetup() {
        when(a1.getName()).thenReturn('a')
        when(b1.getName()).thenReturn('b')
        when(c1.getName()).thenReturn('c')
        when(a2.getName()).thenReturn('a')
        when(b2.getName()).thenReturn('b')
        when(c2.getName()).thenReturn('c')

        when(b2.dependencies).thenReturn(GolangDependencySet.empty())
    }

    void vendorDependencies(GolangDependency... dependencies) {
        GolangDependencySet result = asGolangDependencySet(dependencies)
        when(visitor.visitVendorDependencies(resolvedDependency, rootDir, 'build')).thenReturn(result)
        when(visitor.visitVendorDependencies(resolvedDependency, rootDir, 'test')).thenReturn(result)
    }

    void externalDependencies(GolangDependency... dependencies) {
        GolangDependencySet set = asGolangDependencySet(dependencies)
        when(visitor.visitExternalDependencies(resolvedDependency, rootDir, 'build')).thenReturn(set)
        when(visitor.visitExternalDependencies(resolvedDependency, rootDir, 'test')).thenReturn(set)
    }

    void sourceCodeDependencies(GolangDependency... dependencies) {
        GolangDependencySet set = asGolangDependencySet(dependencies)
        when(visitor.visitSourceCodeDependencies(resolvedDependency, rootDir, 'build')).thenReturn(set)
        when(visitor.visitSourceCodeDependencies(resolvedDependency, rootDir, 'test')).thenReturn(set)
    }


}
