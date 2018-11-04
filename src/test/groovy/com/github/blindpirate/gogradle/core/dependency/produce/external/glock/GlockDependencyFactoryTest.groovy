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

package com.github.blindpirate.gogradle.core.dependency.produce.external.glock

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.AbstractExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class GlockDependencyFactoryTest extends AbstractExternalDependencyFactoryTest {
    @InjectMocks
    GlockDependencyFactory factory

    @Test
    void 'package without GLOCKFILE should be rejected'() {
        assert !factory.canRecognize(resource)
    }

    static final String GLOCKFILE = '''
bitbucket.org/tebeka/selenium 02df1758050f
code.google.com/p/cascadia 4f03c71bc42b
code.google.com/p/go-uuid 7dda39b2e7d5
'''

    @Test
    void 'parsing GLOCKFILE should succeed'() {
        // given
        prepareGlockfile(GLOCKFILE)
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name: 'bitbucket.org/tebeka/selenium', version: '02df1758050f', transitive: false])
        verifyMapParsed([name: 'code.google.com/p/cascadia', version: '4f03c71bc42b', transitive: false])
        verifyMapParsed([name: 'code.google.com/p/go-uuid', version: '7dda39b2e7d5', transitive: false])
    }

    String glockfileWithCmds = '''
cmd code.google.com/p/go.tools/cmd/godoc
cmd code.google.com/p/go.tools/cmd/goimports
cmd code.google.com/p/go.tools/cmd/vet
bitbucket.org/tebeka/selenium 02df1758050f
code.google.com/p/cascadia 4f03c71bc42b
code.google.com/p/go-uuid 7dda39b2e7d5
'''

    @Test
    void 'cmd lines should be ignored'() {
        // given
        prepareGlockfile(glockfileWithCmds)
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verify(mapNotationParser, times(3)).parse(anyMap())
    }

    String glockfileWithCorruptedLine = '''
This is a corrupted line
'''

    @Test(expected = RuntimeException)
    void 'unrecognized line should cause an exception'() {
        // given
        prepareGlockfile(glockfileWithCorruptedLine)
        // then
        factory.produce(parentDependency, resource, 'build')
    }

    void prepareGlockfile(String s) {
        IOUtils.write(resource, "GLOCKFILE", s)
    }
}
