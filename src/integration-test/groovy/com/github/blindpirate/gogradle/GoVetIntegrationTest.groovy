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
