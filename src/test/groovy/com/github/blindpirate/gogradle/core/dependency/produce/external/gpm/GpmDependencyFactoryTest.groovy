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

package com.github.blindpirate.gogradle.core.dependency.produce.external.gpm

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.AbstractExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

@RunWith(GogradleRunner)
class GpmDependencyFactoryTest extends AbstractExternalDependencyFactoryTest {

    @InjectMocks
    GpmDependencyFactory factory

    void writeGodeps() {
        IOUtils.write(resource, 'Godeps', '''
github.com/nu7hatch/gotrail               v0.0.2
github.com/replicon/fast-archiver         v1.02
launchpad.net/gocheck                     r2013.03.03   # Bazaar repositories are supported
code.google.com/p/go.example/hello/...    ae081cd1d6cc  # And so are Mercurial ones
''')
    }

    @Test
    void 'build dependencies should be empty'() {
        // given
        writeGodeps()
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/nu7hatch/gotrail', tag: 'v0.0.2', transitive: false])
        verifyMapParsed([name: 'github.com/replicon/fast-archiver', tag: 'v1.02', transitive: false])
        verifyMapParsed([name: 'launchpad.net/gocheck', tag: 'r2013.03.03', transitive: false])
        verifyMapParsed([name: 'code.google.com/p/go.example/hello/...', version: 'ae081cd1d6cc', transitive: false])
    }

    @Test
    void 'test dependencies should be empty'() {
        writeGodeps()
        assert factory.produce(parentDependency, resource, 'test').isEmpty()
    }

    @Test
    void 'package without Godeps should be rejected'() {
        assert !factory.canRecognize(resource)
    }
}
