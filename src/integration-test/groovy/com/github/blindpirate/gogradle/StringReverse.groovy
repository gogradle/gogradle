package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithProject
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
@WithProject
class StringReverse extends IntegrationTestSupport {
    @Before
    void setUp() {
        IOUtils.write(resource, 'hello.go', helloDotGo)
        writeBuildAndSettingsDotGradle(buildDotGradle)
    }

    @Test
    @AccessWeb
    void 'a simple test with real go code'() {
        newBuild { build ->
            build.forTasks('dependencies')
        }

        assertDependencyOutput()

        buildAgain()

        buildAgainAndAgain()
    }

    void buildAgain() {
        initStdoutStderr()

        newBuild { build ->
            build.forTasks('dependencies')
        }

        assertDependencyOutput()

        assert stdout.toString().contains(":resolveBuildDependencies UP-TO-DATE")
    }

    void buildAgainAndAgain() {
        initStdoutStderr()

        IOUtils.write(resource, 'hello.go', helloDotGo + " ")

        newBuild { build ->
            build.forTasks('dependencies')
        }

        assertDependencyOutput()

        assert !stdout.toString().contains(":resolveBuildDependencies UP-TO-DATE")
    }

    void assertDependencyOutput() {
        // "golang.org/x/tools:0d047c8 √" -> "golang.org/x/tools √"
        assert stdout.toString().replaceAll(/:[a-fA-F0-9]{7}/, '').contains('''
sample
└── github.com/golang/example √
    └── golang.org/x/tools √
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
    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='sample'
}
dependencies {
    build 'github.com/golang/example'
}
"""

    @Override
    File getProjectRoot() {
        return resource
    }
}
