package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
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
        IOUtils.write(resource, 'build.gradle', buildDotGradle)
    }

    @Test
    @AccessWeb
    void 'a simple test with real go code'() {
        newBuild { build ->
            build.forTasks('dependencies')
        }

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
