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

import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Go code in this test is from
 * https://github.com/golang/example/blob/master/hello/hello.go
 */
@RunWith(GogradleRunner)
@WithResource('')
class StringReverse extends IntegrationTestSupport {
    @Before
    void setUp() {
        String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='example'
}
dependencies {
    golang {
        build name: 'github.com/golang/example', commit: '46695d81d1fae905a270fb7db8a4d11a334562fe', subpackages: 'stringutil'
    }
}
"""
        IOUtils.write(resource, 'hello.go', helloDotGo)
        writeBuildAndSettingsDotGradle(buildDotGradle)
    }

    @Test
    @AccessWeb
    void 'build string reverse example should succeed'() {
        newBuild { build ->
            build.forTasks('dependencies', 'build')
        }

        assertDependencyOutput()

        buildAgain()

        buildAgainAndAgain()
    }

    void buildAgain() {
        newBuild { build ->
            build.forTasks('dependencies')
        }

        assertDependencyOutput()

        assert stdout.toString().contains(":resolveBuildDependencies UP-TO-DATE")
    }

    void buildAgainAndAgain() {
        IOUtils.write(resource, 'hello.go', helloDotGo + " ")

        newBuild { build ->
            build.forTasks('dependencies')
        }

        assertDependencyOutput()

        assert !stdout.toString().contains(":resolveBuildDependencies UP-TO-DATE")
    }

    void assertDependencyOutput() {
        assert stdout.toString().contains('''
example
\\-- github.com/golang/example:46695d8 [stringutil]
''')
    }


    String helloDotGo = '''
package main

import (
    "fmt"

    "github.com/golang/example/stringutil"
)

func main() {
    fmt.Println(stringutil.Reverse("!selpmaxe oG ,olleH"))
}
'''


    @Override
    File getProjectRoot() {
        return resource
    }
}
