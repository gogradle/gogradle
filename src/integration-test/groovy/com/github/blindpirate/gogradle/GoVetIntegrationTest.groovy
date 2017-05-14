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
import org.gradle.tooling.BuildException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class GoVetIntegrationTest extends IntegrationTestSupport {
    String mainDotGo
    String buildDotGradle

    @Before
    void setUp() {
        mainDotGo = """
package main

import "fmt"
import "os"

func main() {
    fmt.Println(os.Stderr, fmt.Errorf("error msg"))
}
"""
        buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath="github.com/my/package"
}
"""
        IOUtils.write(resource, 'main.go', mainDotGo)
    }

    @Test
    void 'exception should be thrown if go vet fails'() {
        writeBuildAndSettingsDotGradle(buildDotGradle)
        try {
            newBuild {
                it.forTasks('goVet')
            }
        } catch (BuildException e) {
            assert stderr.toString().contains('first argument to Println is os.Stderr')
            assert stderr.toString().contains('exit status 1')
            assert stdout.toString().contains('goVet FAILED')
        }
    }

    @Test
    void 'exception should be suppressed if continueWhenFail=true'() {
        writeBuildAndSettingsDotGradle(buildDotGradle + '''
goVet {
    continueWhenFail = true
}
''')
        newBuild {
            it.forTasks('goVet')
        }
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
