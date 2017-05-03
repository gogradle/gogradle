package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithGitRepo
import com.github.blindpirate.gogradle.support.WithIsolatedUserhome
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// https://github.com/blindpirate/gogradle/issues/87
@RunWith(GogradleRunner)
@WithResource('')
@WithIsolatedUserhome
@WithGitRepo(repoName = 'a', fileName = 'vendor/github.com/user/b/b.go')
class ResolvingHostWithoutContextIntegrationTest extends IntegrationTestSupport {

    @Override
    File getProjectRoot() {
        return resource
    }

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='github.com/my/package'
}

repositories {
    golang {
        root 'github.com/user/a'
        url 'http://localhost:8080/a'
    }
}

dependencies {
    golang {
        build 'github.com/user/a'
    }
}
""")
    }

    @Test
    void 'test'() {
        newBuild {
            it.forTasks('goLock')
        }

        newBuild {
            it.forTasks('installBuildDependencies')
        }

        assert new File(resource, '.gogradle/build_gopath/src/github.com/user/b/b.go').exists()
    }
}
