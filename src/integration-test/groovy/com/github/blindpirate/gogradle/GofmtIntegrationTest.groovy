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

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class GofmtIntegrationTest extends IntegrationTestSupport {

    String mainDotGo
    String subDotGo
    String buildDotGradle


    @Before
    void setUp() {
        mainDotGo = '''\
package main

import "fmt"

func main() {
fmt.Printf("hello world")
return 0
}
'''
        subDotGo = '''\
package sub 

import "fmt"

func sub() {
fmt.Printf("hello world")
return 0
}
'''

        buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath="github.com/my/package"
}
"""
        IOUtils.write(resource, 'main.go', mainDotGo)
        IOUtils.write(resource, 'sub/sub.go', subDotGo)
        writeBuildAndSettingsDotGradle(buildDotGradle)
    }

    @Test
    void 'gofmt should succeed'() {
        newBuild {
            it.forTasks('gofmt')
        }

        assert new File(resource, 'main.go').getText() == '''\
package main

import "fmt"

func main() {
\tfmt.Printf("hello world")
\treturn 0
}
'''
        assert new File(resource, 'sub/sub.go').getText() == '''\
package sub

import "fmt"

func sub() {
\tfmt.Printf("hello world")
\treturn 0
}
'''
    }

    @Test
    void 'customized gofmt should not affect other files'() {
        writeBuildAndSettingsDotGradle(buildDotGradle + '''
gofmt {
    gofmt '-w main.go'
}
''')
        newBuild {
            it.forTasks('gofmt')
        }

        assert new File(resource, 'sub/sub.go').getText() == subDotGo
    }

    @Override
    File getProjectRoot() {
        return resource
    }

}
