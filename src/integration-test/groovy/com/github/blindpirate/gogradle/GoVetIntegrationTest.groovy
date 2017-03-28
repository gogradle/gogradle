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

    String mainDotGo = """
package main

import "fmt"
import "os"

func main() {
    fmt.Println(os.Stderr, fmt.Errorf("error msg"))
}
"""
    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath="github.com/my/package"
}
"""

    @Before
    void setUp() {
        IOUtils.write(resource, 'main.go', mainDotGo)
    }

    @Test
    void 'exception should be thrown if go vet fails'() {
        writeBuildAndSettingsDotGradle(buildDotGradle)
        try {
            newBuild {
                it.forTasks('vet')
            }
        } catch (BuildException e) {
            assert stderr.toString().contains('first argument to Println is os.Stderr')
            assert stderr.toString().contains('exit status 1')
            assert stdout.toString().contains('vet FAILED')
        }
    }

    @Test
    void 'exception should be suppressed if continueWhenFail=true'() {
        writeBuildAndSettingsDotGradle(buildDotGradle + '''
vet {
    continueWhenFail = true
}
''')
        newBuild {
            it.forTasks('vet')
        }
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
