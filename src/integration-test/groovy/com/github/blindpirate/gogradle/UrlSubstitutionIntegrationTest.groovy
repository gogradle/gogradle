package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithGitRepo
import com.github.blindpirate.gogradle.support.WithIsolatedUserhome
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
@WithIsolatedUserhome
@WithGitRepo(repoName = 'myawesomeproject', fileName = 'main.go')
class UrlSubstitutionIntegrationTest extends IntegrationTestSupport {

    @Before
    void setUp() {
        String buildDotGradle = """
System.setProperty('gradle.user.home',"${StringUtils.toUnixString(userhome)}")
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}

apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath='my/project'
    goExecutable='${goBinPath}'
}

repositories {
    golang {
        all()
        urlSubstitution {
            'http://localhost:8080/myawesomeproject'
        }
    }
}

dependencies {
    build 'my/awesome/project'
}
"""
        writeBuildAndSettingsDotGradle(buildDotGradle)
    }

    @Override
    File getProjectRoot() {
        return resource
    }


    @Test
    void 'url substitution should succeed'() {
        try {
            newBuild {
                it.forTasks('resolveBuildDependencies')
            }
        } finally {
            println(stderr)
            println(stdout)
        }

        assert new File(resource, ".gogradle/build_gopath/src/my/awesome/project/main.go").exists()
    }
}
