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

package com.github.blindpirate.gogradle.core.dependency.produce.external.glide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

@RunWith(GogradleRunner)
class GlideDependencyFactoryTest extends ExternalDependencyFactoryTest {

    @InjectMocks
    GlideDependencyFactory factory

    @Test
    void 'test dependencies should be empty'() {
        prepareGlideDotLock(glideDotLock)
        factory.produce(resource, 'test')
        verifyMapParsed([name: 'test', version: 'testVersion', transitive: false])
    }

    @Test
    void 'package without glide.lock should be rejected'() {
        assert !factory.canRecognize(resource)
    }

    String glideDotLock = '''
hash: 67c5571c33bfcb663d32d2b40b9ce1f2a05a3fa2e9f442077277c2782195729c
updated: 2016-08-11T14:22:17.773372627-04:00
imports:
- name: github.com/codegangsta/cli
  version: 1efa31f08b9333f1bd4882d61f9d668a70cd902e
- name: github.com/Masterminds/semver
  version: 8d0431362b544d1a3536cca26684828866a7de09
- name: github.com/Masterminds/vcs
  version: fbe9fb6ad5b5f35b3e82a7c21123cfc526cbf895
- name: gopkg.in/yaml.v2
  version: e4d366fc3c7938e2958e662b4258c7a89e1f0e3e
  subpackages:
    - memcache
    - redis
testImports: 
- name: test
  version: testVersion 
'''

    @Test
    void 'parsing glide.lock should succeed'() {
        // given
        prepareGlideDotLock(glideDotLock)

        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/codegangsta/cli', version: '1efa31f08b9333f1bd4882d61f9d668a70cd902e', transitive: false])
        verifyMapParsed([name: 'github.com/Masterminds/semver', version: '8d0431362b544d1a3536cca26684828866a7de09', transitive: false])
        verifyMapParsed([name: 'github.com/Masterminds/vcs', version: 'fbe9fb6ad5b5f35b3e82a7c21123cfc526cbf895', transitive: false])
        verifyMapParsed([name       : 'gopkg.in/yaml.v2',
                         version    : 'e4d366fc3c7938e2958e662b4258c7a89e1f0e3e',
                         subpackages: ['memcache', 'redis'],
                         transitive : false])
    }

    String glideDotLockMissingName = '''
hash: 67c5571c33bfcb663d32d2b40b9ce1f2a05a3fa2e9f442077277c2782195729c
updated: 2016-08-11T14:22:17.773372627-04:00
imports:
- version: 1efa31f08b9333f1bd4882d61f9d668a70cd902e
testImports: []
'''

    @Test(expected = IllegalStateException)
    void 'missing name should cause an exception'() {
        // given
        prepareGlideDotLock(glideDotLockMissingName)
        // then
        factory.produce(resource, 'build')
    }

    String glideDotLockMissingVersion = '''
hash: 67c5571c33bfcb663d32d2b40b9ce1f2a05a3fa2e9f442077277c2782195729c
updated: 2016-08-11T14:22:17.773372627-04:00
imports:
- name: github.com/codegangsta/cli
testImports: []
'''

    @Test
    void 'missing version should not cause an exception'() {
        // given
        prepareGlideDotLock(glideDotLockMissingVersion)
        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/codegangsta/cli', transitive: false])
    }

    String glideDotLockWithExtraAndMissingProperties = '''
wtf: This is an extra property
imports:
- name: github.com/codegangsta/cli
  version: 1efa31f08b9333f1bd4882d61f9d668a70cd902e
  wtf: xxx
'''

    @Test
    void 'extra properties in glide.lock should succeed'() {
        // given
        prepareGlideDotLock(glideDotLockWithExtraAndMissingProperties)
        // when
        factory.produce(resource, 'build')
        // then

        verifyMapParsed([name      : 'github.com/codegangsta/cli',
                         version   : '1efa31f08b9333f1bd4882d61f9d668a70cd902e',
                         transitive: false])
    }

    @Test
    void 'corrupt glide.lock should not cause exception'() {
        prepareGlideDotLock('hash: xxx')
        assert factory.produce(resource, 'build').isEmpty()
        assert factory.produce(resource, 'test').isEmpty()
    }

    void prepareGlideDotLock(String glideDotLock) {
        IOUtils.write(resource, 'glide.lock', glideDotLock)
    }
}
