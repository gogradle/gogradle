package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.*
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
${buildDotGradleBase}
golang {
    packagePath='my/project'
}

repositories {
    golang {
        all()
        url {
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
