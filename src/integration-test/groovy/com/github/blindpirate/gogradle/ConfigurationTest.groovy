package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
class ConfigurationTest extends IntegrationTestSupport {

    @Before
    void setUp() {
        IOUtils.mkdir(resource, 'a')
        IOUtils.mkdir(resource, 'b')
        IOUtils.mkdir(resource, 'c')
        IOUtils.mkdir(resource, 'd')

        IOUtils.mkdir(resource, 'project')
        IOUtils.write(resource, 'project/main.go', 'package main\nimport "a"')
        IOUtils.write(resource, 'project/main_test.go', 'package main\nimport "c"')

        IOUtils.write(resource, 'a/main.go', 'package main\nimport"b"')
        IOUtils.write(resource, 'a/main_test.go', 'package main\nimport"unused"')

        IOUtils.write(resource, 'b/main.go', 'package main\nimport"fmt"')
        IOUtils.write(resource, 'b/main_test.go', 'package main\nimport"unused"')

        IOUtils.write(resource, 'c/main.go', 'package main\nimport"d"')
        IOUtils.write(resource, 'c/main_test.go', 'package main\nimport"unused"')

        IOUtils.write(resource, 'd/main.go', 'package main\nimport"fmt"')
        IOUtils.write(resource, 'd/main_test.go', 'package main\nimport"unused"')

        writeBuildAndSettingsDotGradle("""
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
golang {
    goExecutable = '${StringUtils.toUnixString(goBinPath)}'
    packagePath= 'github.com/my/project'
}

repositories {
    golang {
        root {['a','b','c','d'].contains(it)}
        dir {
            '${StringUtils.toUnixString(resource.absolutePath)}'+'/'+it
        }
    }
}
""")
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'project')
    }

    @Test
    void 'dependencies should be resolved correctly'() {
        try {
            newBuild {
                it.forTasks('dependencies')
            }

            String actualOutput = stdout.toString().replaceAll('\\r', '')
            String expectedOutput = """
build:
github.com/my/project
└── a:${StringUtils.toUnixString(resource)}/a √
    └── b:${StringUtils.toUnixString(resource)}/b √

test:
github.com/my/project
└── c:${StringUtils.toUnixString(resource)}/c √
    └── d:${StringUtils.toUnixString(resource)}/d √
""".replaceAll('\\r', '')

            assert actualOutput.concat(expectedOutput)
        } finally {
            println(stdout)
            println(stderr)
        }
    }
}
