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
import com.github.blindpirate.gogradle.util.StringUtils
import org.gradle.tooling.BuildException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class GoVetIntegrationTest extends IntegrationTestSupport {
    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath="github.com/my/package"
}
dependencies {
    golang {
        build name:'github.com/my/a', dir:'${StringUtils.toUnixString(resource)}/.tmp'
    }
}
""")
        IOUtils.mkdir(resource, '.tmp')
    }

    String goFileWithVetError = """
package main

import "fmt"
import "os"

func main() {
    fmt.Println(os.Stderr, fmt.Errorf("error msg"))
}
"""


    void writeGoFileWithErrorToProjectRoot() {
        IOUtils.write(resource, 'main.go', goFileWithVetError)
    }

    void writeGoFileWithErrorToVendor() {
        IOUtils.write(resource, 'vendor/main.go', goFileWithVetError)
    }

    void writeGoFileWithErrorToSub() {
        IOUtils.write(resource, 'sub/main.go', goFileWithVetError)
    }

    @Test
    void 'exception should be thrown if error exists in project root'() {
        writeGoFileWithErrorToProjectRoot()
        try {
            newBuild {
                it.forTasks('vet')
            }
        } catch (BuildException e) {
            assert stderr.toString().contains('first argument to Println is os.Stderr')
            assert stdout.toString().contains('vet FAILED')
        }
    }

    @Test
    void 'exception should be thrown if error exists in sub package'() {
        writeGoFileWithErrorToSub()
        try {
            newBuild {
                it.forTasks('vet')
            }
        } catch (BuildException e) {
            assert stderr.toString().contains('first argument to Println is os.Stderr')
            assert stdout.toString().contains('vet FAILED')
        }
    }

    @Test
    void 'exception should be suppressed if continueOnFailure=true'() {
        writeGoFileWithErrorToProjectRoot()
        IOUtils.append(new File(resource, 'build.gradle'), '''
vet {
    continueOnFailure = true
}
''')
        newBuild {
            it.forTasks('vet')
        }
    }

    @Test
    void 'code in vendor should not be vetted'() {
        writeGoFileWithErrorToVendor()
        newBuild {
            it.forTasks('vet')
        }
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
