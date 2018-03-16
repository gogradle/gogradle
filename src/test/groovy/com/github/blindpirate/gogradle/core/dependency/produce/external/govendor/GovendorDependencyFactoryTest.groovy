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

package com.github.blindpirate.gogradle.core.dependency.produce.external.govendor

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.AbstractExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.core.pack.GithubGitlabPackagePathResolver
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mockito

@RunWith(GogradleRunner)
class GovendorDependencyFactoryTest extends AbstractExternalDependencyFactoryTest {
    @InjectMocks
    GovendorDependencyFactory factory

    String vendorDotJson = '''
            {
                "comment": "",
                "ignore": "test",
                "package": [
                    {
                        "checksumSHA1": "rcwA7Jmo3eZ4bEQb8mTI78haZfc=",
                        "path": "github.com/Bowery/prompt",
                        "revision": "d43c2707a6c5a152a344c64bb4fed657e2908a81",
                        "revisionTime": "2016-08-08T16:52:56Z"
                    },
                    {
                        "checksumSHA1": "6VGFARaK8zd23IAiDf7a+gglC8k=",
                        "path": "github.com/dchest/safefile",
                        "revision": "855e8d98f1852d48dde521e0522408d1fe7e836a",
                        "revisionTime": "2015-10-22T12:31:44+02:00"
                    },
                    {
                        "checksumSHA1": "CujWu7+PWlZSX5+zAPJH91O5AVQ=",
                        "origin": "github.com/docker/distribution/vendor/github.com/Sirupsen/logrus",
                        "path": "github.com/Sirupsen/logrus",
                        "revision": "0700fa570d7bcc1b3e46ee127c4489fd25f4daa3",
                        "revisionTime": "2017-03-21T17:14:25Z"
                    },
                    {
                        "path": "plugin",
                        "revision": ""
                    },
                    {
                        "path": "github.com/my/package",
                        "revision" : "whatever"
                    },
                    {
                        "path": "github.com/target/package",
                        "revision" : "whatever"
                    }
            ],
                "rootPath": "github.com/kardianos/govendor"
        }
    '''

    @Before
    void setUp() {
        Mockito.when(parentDependency.getName()).thenReturn("github.com/my/package")
        ReflectionUtils.setField(factory, 'packagePathResolver', new GithubGitlabPackagePathResolver("github.com"))
    }

    @Test
    void 'package without vendor/vendor.json should be rejected'() {
        assert !factory.canRecognize(resource)
    }

    @Test
    void 'reading vendor/vendor.json should succeed'() {
        // given
        prepareVendorDotJson(vendorDotJson)
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name      : 'github.com/Bowery/prompt',
                         version   : 'd43c2707a6c5a152a344c64bb4fed657e2908a81',
                         transitive: false])
        verifyMapParsed([name      : 'github.com/dchest/safefile',
                         version   : '855e8d98f1852d48dde521e0522408d1fe7e836a',
                         transitive: false])
        verifyMapParsed([name      : 'github.com/Sirupsen/logrus',
                         host      : [name   : 'github.com/docker/distribution',
                                      version: '0700fa570d7bcc1b3e46ee127c4489fd25f4daa3'],
                         vendorPath: 'vendor/github.com/Sirupsen/logrus',
                         transitive: false])
    }

    @Test(expected = RuntimeException)
    void 'corrupted vendor.json should cause an exception'() {
        // given
        prepareVendorDotJson('This is corrupted')
        // then
        factory.produce(parentDependency, resource, 'build')
    }

    @Test(expected = IllegalStateException)
    void 'missing path should cause an exception'() {
        // given
        prepareVendorDotJson('{"package":[{}]}')
        // then
        factory.produce(parentDependency, resource, 'build')
    }

    @Test
    void 'missing revision should not cause an exception'() {
        // given
        prepareVendorDotJson('{"package":[{"path":"a"}]}')
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name: 'a', transitive: false])
    }

    String vendorDotJsonWithExtraProperties = '''
    {
                "comment": "",
                "ignore": "test",
                "a":[],
                "package": [
                    {
                        "checksumSHA1": "rcwA7Jmo3eZ4bEQb8mTI78haZfc=",
                        "path": "github.com/Bowery/prompt",
                        "revision": "d43c2707a6c5a152a344c64bb4fed657e2908a81",
                        "revisionTime": "2016-08-08T16:52:56Z",
                        "a":"1"
                    } 
            ],
                "rootPath": "github.com/kardianos/govendor"
        }
'''

    @Test
    void 'extra properties should be ignored'() {
        // given
        prepareVendorDotJson(vendorDotJsonWithExtraProperties)
        // then
        factory.produce(parentDependency, resource, 'build')
    }

    void prepareVendorDotJson(String s) {
        IOUtils.write(resource, "vendor/vendor.json", s)
    }
}
