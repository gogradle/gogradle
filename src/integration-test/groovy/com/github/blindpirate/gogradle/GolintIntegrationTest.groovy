package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@OnlyWhen(value = '"golint".execute()', ignoreTestWhenException = OnlyWhen.ExceptionStrategy.TRUE)
class GolintIntegrationTest extends IntegrationTestSupport {

    File resource

    String buildDotGradle = """
${buildDotGradleBase}

golang {
    packagePath='a/b/c'
}

task golint(type: com.github.blindpirate.gogradle.Go){
    doLast {
        run 'golint a/b/c'
    }
}
"""
    String mainDotGo = '''
package main

import . "fmt"

func main(){
fmt.Printf("Hello")
}
'''

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle(buildDotGradle)
        IOUtils.write(resource, 'main.go', mainDotGo)
    }

    @Test
    void 'golint should succeed'() {
        try {
            newBuild {
                it.forTasks('golint')
            }
        } finally {
            assert stdout.toString().contains('should not use dot imports')
        }
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
