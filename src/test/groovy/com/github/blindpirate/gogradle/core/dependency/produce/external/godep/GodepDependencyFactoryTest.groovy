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

package com.github.blindpirate.gogradle.core.dependency.produce.external.godep

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.AbstractExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

@RunWith(GogradleRunner)
class GodepDependencyFactoryTest extends AbstractExternalDependencyFactoryTest {
    @InjectMocks
    GodepDependencyFactory godepDependencyFactory

    @Test
    void 'package with Godeps Godeps_json should be rejected'() {
        // then:
        assert !godepDependencyFactory.canRecognize(resource)
    }

    String godepsDotJson = '''
            {
                "ImportPath": "github.com/tools/godep",
                "GoVersion": "go1.7",
                "GodepVersion": "v74",
                "Deps": [
                    {
                        "ImportPath": "github.com/kr/fs",
                        "Rev": "2788f0dbd16903de03cb8186e5c7d97b69ad387b"
                    },
                    {
                        "ImportPath": "github.com/kr/pretty",
                        "Comment": "go.weekly.2011-12-22-24-gf31442d",
                        "Rev": "f31442d60e51465c69811e2107ae978868dbea5c"
                    }
            ]
            }
            '''

    @Test
    void 'package with Godeps Godeps_json should be analyzed properly'() {
        // given:
        prepareGodepsDotJson(godepsDotJson)

        // when:
        godepDependencyFactory.produce(parentDependency, resource, 'build')
        // then:
        verifyMapParsed([name      : "github.com/kr/fs",
                         version   : '2788f0dbd16903de03cb8186e5c7d97b69ad387b',
                         transitive: false])
        verifyMapParsed([name      : "github.com/kr/pretty",
                         version   : 'f31442d60e51465c69811e2107ae978868dbea5c',
                         transitive: false])
    }

    @Test(expected = RuntimeException)
    void 'corrupted Godeps_json should result in an exception'() {
        // given
        prepareGodepsDotJson('This is a corrupted Godeps.json')
        // then
        godepDependencyFactory.produce(parentDependency, resource, 'build')
    }

    @Test(expected = IllegalStateException)
    void 'blank ImportPath should cause an exception'() {
        // given
        prepareGodepsDotJson('''
{
    "Deps":[{"ImportPath":"  "}]
}
''')
        // then
        godepDependencyFactory.produce(parentDependency, resource, 'build')
    }

    @Test
    void 'missing Rev should not cause an exception'() {
        // given
        prepareGodepsDotJson('''
{
    "Deps":[{"ImportPath":"a"}]
}
''')
        // when
        godepDependencyFactory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name: 'a', transitive: false])

    }

    void prepareGodepsDotJson(String godepsDotJson) {
        IOUtils.write(resource, 'Godeps/Godeps.json', godepsDotJson)
    }

    String godepsDotJsonWithExtraAndMissingProperties = '''
            {
                "extraProperties":[1,"a",{"c":null}],
                "ImportPath": "github.com/tools/godep",
                "Deps": [{"a":1,"ImportPath":"a","Rev":"b"}]
            }
            '''

    @Test
    void 'extra properties in Godeps Godeps_json should be ignored'() {
        // given
        prepareGodepsDotJson(godepsDotJsonWithExtraAndMissingProperties)
        // when
        godepDependencyFactory.produce(parentDependency, resource, 'build')
        // then
        verifyMapParsed([name: 'a', version: 'b', transitive: false])
    }

}
