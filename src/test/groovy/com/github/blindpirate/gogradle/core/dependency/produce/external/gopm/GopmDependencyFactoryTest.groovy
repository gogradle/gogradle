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

package com.github.blindpirate.gogradle.core.dependency.produce.external.gopm

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

@RunWith(GogradleRunner)
class GopmDependencyFactoryTest extends ExternalDependencyFactoryTest {
    @InjectMocks
    GopmDependencyFactory factory

    String dotGopmfile = '''
[target]
path = github.com/gogits/gogs

[deps]
github.com/a/b =
github.com/c/d
github.com/e/f = /path/to/my/project
github.com/g/h = d:\\projects\\xorm
golang.org/i/j = branch:master
golang.org/k/l = tag:v0.9.0
gopkg.in/redis.v2 = commit:e617904

[res]
include = public|scripts|templates'''

    @Test
    void 'parsing .gopmfile should succeed'() {
        // given
        IOUtils.write(resource, '.gopmfile', dotGopmfile)
        // when
        factory.produce(resource, 'build')

        // then
        verifyMapParsed([name: 'github.com/a/b'])
        verifyMapParsed([name: 'github.com/c/d'])
        verifyMapParsed([name: 'github.com/e/f', dir: '/path/to/my/project'])
        verifyMapParsed([name: 'github.com/g/h', dir: /d:\projects\xorm/])
        verifyMapParsed([name: 'golang.org/i/j', branch: 'master'])
        verifyMapParsed([name: 'golang.org/k/l', tag: 'v0.9.0'])
        verifyMapParsed([name: 'gopkg.in/redis.v2', commit: 'e617904'])
    }

    @Test
    void 'missing [deps] should result in empty list'() {
        // given
        IOUtils.write(resource, '.gopmfile', '[target]\npath = github.com/gogits/gogs')
        // then
        assert factory.produce(resource, 'build').isEmpty()
    }

    @Test
    void 'empty [deps] should result in empty list'() {
        // given
        IOUtils.write(resource, '.gopmfile', '''[target]
path = github.com/gogits/gogs
[deps]
''')
        // then
        assert factory.produce(resource, 'build').isEmpty()
    }

    String misorderedDotGompfile1 = '''
[deps]
github.com/c/d

[target]
path = github.com/gogits/gogs

[res]
include = public|scripts|templates'''

    String misorderedDotGompfile2 = '''
[target]
path = github.com/gogits/gogs

[res]
include = public|scripts|templates

[deps]
github.com/c/d
'''
    String misorderedDotGompfile3 = '''
[res]
include = public|scripts|templates

[deps]
github.com/c/d

[target]
path = github.com/gogits/gogs
'''


    @Test
    void 'misordered section should not affect result - 1'() {
        misorderTest(misorderedDotGompfile1)
    }

    @Test
    void 'misordered section should not affect result - 2'() {
        misorderTest(misorderedDotGompfile2)
    }

    @Test
    void 'misordered section should not affect result - 3'() {
        misorderTest(misorderedDotGompfile3)
    }

    void misorderTest(String dotGopmfile) {
        // given
        IOUtils.write(resource, '.gopmfile', dotGopmfile)
        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/c/d'])
    }

}
