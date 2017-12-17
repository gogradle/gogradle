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

package com.github.blindpirate.gogradle.core.dependency.produce.external.gvtgbvendor

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks


@RunWith(GogradleRunner)
class GvtGbvendorDependencyFactoryTest extends ExternalDependencyFactoryTest {
    @InjectMocks
    GvtGbvendorDependencyFactory factory

    String manifest = '''
            {
                "generator": "github.com/FiloSottile/gvt",
                "dependencies": [
                    {
                        "importpath": "github.com/wadey/gocovmerge",
                        "repository": "https://github.com/wadey/gocovmerge",
                        "vcs": "git",
                        "revision": "b5bfa59ec0adc420475f97f89b58045c721d761c",
                        "branch": "master",
                        "notests": true
                    },
                    {
                        "importpath": "golang.org/x/tools/cmd/goimports",
                        "repository": "https://go.googlesource.com/tools",
                        "vcs": "git",
                        "revision": "8b84dae17391c154ca50b0162662aa1fc9ff84c2",
                        "branch": "master",
                        "path": "/cmd/goimports",
                        "notests": true
                    }
            ]
            }
            '''

    @Test
    void 'reading vendor/manifest should succeed'() {
        // given
        IOUtils.write(resource, 'vendor/manifest', manifest)
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name      : 'github.com/wadey/gocovmerge',
                         vcs       : 'git',
                         version   : 'b5bfa59ec0adc420475f97f89b58045c721d761c',
                         transitive: false])
        verifyMapParsed([name      : 'golang.org/x/tools',
                         vcs       : 'git',
                         version   : '8b84dae17391c154ca50b0162662aa1fc9ff84c2',
                         transitive: false])
    }

    @Test
    void 'directory without vendor/manifest should be rejected'() {
        assert !factory.canRecognize(resource)
    }

    String manifestMissingImportpath = '''
           {
                "generator": "github.com/FiloSottile/gvt",
                "dependencies": [
                    {
                        "repository": "https://github.com/wadey/gocovmerge",
                        "vcs": "git",
                        "revision": "b5bfa59ec0adc420475f97f89b58045c721d761c",
                        "branch": "master",
                        "notests": true
                    } 
            ]
            }
'''

    @Test(expected = RuntimeException)
    void 'missing importpath will result in an exception'() {
        // given
        IOUtils.write(resource, 'vendor/manifest', manifestMissingImportpath)
        // then
        factory.produce(parentDependency, resource, 'build')
    }

    String manifestWithExtraProperties = '''
            {
                "generator": "github.com/FiloSottile/gvt",
                "extra":1,
                "dependencies": [
                    {
                        "importpath": "github.com/wadey/gocovmerge",
                        "repository": "https://github.com/wadey/gocovmerge",
                        "vcs": "git",
                        "revision": "b5bfa59ec0adc420475f97f89b58045c721d761c",
                        "branch": "master",
                        "notests": true,
                        "extra":{}
                    },
                    {
                        "importpath": "golang.org/x/tools/cmd/goimports",
                        "repository": "https://go.googlesource.com/tools",
                        "vcs": "git",
                        "revision": "8b84dae17391c154ca50b0162662aa1fc9ff84c2",
                        "branch": "master",
                        "path": "/cmd/goimports",
                        "notests": true
                    }
                ]
            }
            '''

    @Test
    void 'extra properties should be ignored'() {
        // given
        IOUtils.write(resource, 'vendor/manifest', manifestWithExtraProperties)
        // when
        factory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name      : 'github.com/wadey/gocovmerge',
                         vcs       : 'git',
                         version   : 'b5bfa59ec0adc420475f97f89b58045c721d761c',
                         transitive: false])
        verifyMapParsed([name      : 'golang.org/x/tools',
                         vcs       : 'git',
                         version   : '8b84dae17391c154ca50b0162662aa1fc9ff84c2',
                         transitive: false])

    }

}
