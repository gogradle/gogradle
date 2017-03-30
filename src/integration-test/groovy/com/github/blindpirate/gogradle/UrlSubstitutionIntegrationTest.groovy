package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.GitServer
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
class UrlSubstitutionIntegrationTest extends IntegrationTestSupport {

    File projectRoot
    File repoRoot
    GitServer server

    @Before
    void setUp() {
        String buildDotGradle = """
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
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
        projectRoot = IOUtils.mkdir(resource, 'projectRoot')
        repoRoot = IOUtils.mkdir(resource, 'repoRoot')

        GitServer.createRepository(repoRoot, 'main.go')
        server = GitServer.newServer()
        server.addRepo('myawesomeproject', repoRoot)
        server.start(GitServer.DEFAULT_PORT)

        IOUtils.write(projectRoot, 'build.gradle', buildDotGradle)
    }

    @After
    void clearUp() {
        server.stop()
    }

    @Override
    File getProjectRoot() {
        return projectRoot
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

        assert new File(projectRoot, ".gogradle/build_gopath/src/my/awesome/project/main.go").exists()
    }
}
