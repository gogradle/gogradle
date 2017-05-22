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

        IOUtils.write(resource, 'main.go', """
package main

import "fmt"
import "os"
import "github.com/my/a"
""")

        IOUtils.write(resource, '.tmp/a.go', """
package a

import "fmt"
import "os"

func A() {
    fmt.Println(os.Stderr, fmt.Errorf("error msg"))
}
""")
    }

    void letGoVetForMainFail() {
        IOUtils.append(new File(resource, 'main.go'), """
func main() {
    fmt.Println(os.Stderr, fmt.Errorf("error msg"))
}
""")
    }

    @Test
    void 'exception should be thrown if go vet fails'() {
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
        letGoVetForMainFail()
        IOUtils.append(new File(resource, 'build.gradle'), '''
goVet {
    continueWhenFail = true
}
''')
        newBuild {
            it.forTasks('goVet')
        }
    }

    @Test
    void 'code in vendor should not be vetted'() {
        newBuild {
            it.forTasks('goVet')
        }
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
