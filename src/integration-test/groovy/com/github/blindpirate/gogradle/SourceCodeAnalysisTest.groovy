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

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.GogradleModuleSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.google.inject.Inject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('example-master.zip')
class SourceCodeAnalysisTest extends GogradleModuleSupport {

    @Inject
    SourceCodeDependencyFactory factory

    File resource

    @Mock
    ResolvedDependency resolvedDependency

    @Test
    @AccessWeb
    void 'imports should be parsed correctly'() {
        assertImportsAre(['...'] as Set, ['golang.org/x/tools', 'github.com/golang/example'] as Set)
        assertImportsAre(['.'] as Set, [] as Set)
        assertImportsAre(['.', 'hello'] as Set, ['github.com/golang/example'] as Set)
        assertImportsAre(['.', 'hello', 'gotypes/skeleton'] as Set, ['golang.org/x/tools', 'github.com/golang/example'] as Set)
    }

    void assertImportsAre(Set subpackages, Set expect) {
        // given
        when(resolvedDependency.getName()).thenReturn("name")
        when(resolvedDependency.getSubpackages()).thenReturn(subpackages as Set)

        // when
        GolangDependencySet result = factory.produce(resolvedDependency, resource, 'build')

        // then
        assert result.collect { it.name } as Set == expect
    }
}
