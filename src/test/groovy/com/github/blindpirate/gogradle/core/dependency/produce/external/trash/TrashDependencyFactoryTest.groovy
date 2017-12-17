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

package com.github.blindpirate.gogradle.core.dependency.produce.external.trash

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks


@RunWith(GogradleRunner)
class TrashDependencyFactoryTest extends ExternalDependencyFactoryTest {

    @InjectMocks
    TrashDependencyFactory factory

    @Test
    void 'parsing vendor.conf should succeed'() {
        // given
        IOUtils.write(resource, 'vendor.conf', '''
# package
github.com/rancher/trash

github.com/Sirupsen/logrus                      v0.10.0
github.com/cloudfoundry-incubator/candiedyaml   99c3df8
github.com/coreos/go-systemd v4
github.com/go-check/check 4ed411733c5785b40214c70bce814c3a3a689609 https://github.com/cpuguy83/check.git
github.com/opencontainers/runtime-spec 1c7c27d043c2a5e513a44084d2b10d77d1402b8c # specs
''')
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/Sirupsen/logrus', tag: 'v0.10.0', transitive: false])
        verifyMapParsed([name: 'github.com/cloudfoundry-incubator/candiedyaml', version: '99c3df8', transitive: false])
        verifyMapParsed([name: 'github.com/coreos/go-systemd', tag: 'v4', transitive: false])
        verifyMapParsed([name      : 'github.com/go-check/check',
                         version   : '4ed411733c5785b40214c70bce814c3a3a689609',
                         url       : 'https://github.com/cpuguy83/check.git',
                         transitive: false])
        verifyMapParsed([name: 'github.com/opencontainers/runtime-spec', version: '1c7c27d043c2a5e513a44084d2b10d77d1402b8c', transitive: false])

    }

    @Test
    void 'parsing vendor.conf as yaml should succeed'() {
        // given
        IOUtils.write(resource, 'vendor.conf', '''
import:
- package: github.com/Sirupsen/logrus               # package name
  version: v0.8.7                                   # tag or commit
  repo:    https://github.com/imikushin/logrus.git  # (optional) git URL

- package: github.com/codegangsta/cli
  version: b5232bb2934f606f9f27a1305f1eea224e8e8b88

- package: github.com/cloudfoundry-incubator/candiedyaml
  version: 55a459c2d9da2b078f0725e5fb324823b2c71702
''')
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name: 'github.com/Sirupsen/logrus', tag: 'v0.8.7', url: 'https://github.com/imikushin/logrus.git', transitive: false])
        verifyMapParsed([name: 'github.com/codegangsta/cli', version: 'b5232bb2934f606f9f27a1305f1eea224e8e8b88', transitive: false])
        verifyMapParsed([name: 'github.com/cloudfoundry-incubator/candiedyaml', version: '55a459c2d9da2b078f0725e5fb324823b2c71702', transitive: false])
    }
}
