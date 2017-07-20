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

package com.github.blindpirate.gogradle.core.exceptions

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class ResolutionStackWrappingExceptionTest {

    @Mock
    ResolveContext root
    @Mock
    ResolveContext sub
    @Mock
    ResolveContext subsub

    @Mock
    GogradleRootProject rootDependency
    @Mock
    NotationDependency subNotationDependency
    @Mock
    ResolvedDependency subResolvedDependency
    @Mock
    NotationDependency subsubNotationDependency
    @Mock
    ResolvedDependency subsubResolvedDependency

    @Before
    void setUp() {
        when(root.dependency).thenReturn(rootDependency)
        when(rootDependency.resolve(null)).thenReturn(rootDependency)
        when(rootDependency.toString()).thenReturn('root')

        when(sub.getParent()).thenReturn(root)
        when(sub.dependency).thenReturn(subNotationDependency)
        when(subNotationDependency.resolve(null)).thenReturn(subResolvedDependency)
        when(subResolvedDependency.toString()).thenReturn("sub")

        when(subsub.getParent()).thenReturn(sub)
        when(subsub.dependency).thenReturn(subsubNotationDependency)
        when(subsubNotationDependency.resolve(null)).thenReturn(subResolvedDependency)
        when(subsubResolvedDependency.toString()).thenReturn("subsub")
    }

    @Test
    void 'printing resolution stack should succeed'() {
        assert ResolutionStackWrappingException.wrapWithResolutionStack(new Exception("message"), subsub).message.contains('''\
message
Resolution stack is:
+- root
 +- sub
  +- sub''')
    }

}
