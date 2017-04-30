package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithGitRepos
import com.github.blindpirate.gogradle.support.WithIsolatedUserhome
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithGitRepos(repoNames = ['a', 'b', 'c'], fileNames = ['a.go', 'b.go', 'c.go'])
@WithResource('')
@WithMockGo
@WithIsolatedUserhome
class IncrementalInstallationTest extends IntegrationTestSupport {
    File resource

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='github.com/my/project'
}

repositories {
    golang {
        root ~/localhost.\\w/
        url {it.replace('localhost','http://localhost:8080')}
    }
}

dependencies {
    golang {
        build 'localhost/a'
        build 'localhost/b'
    }
}
""")
    }

    @Test
    void 'incremental installation should succeed'() {
        newBuild {
            it.forTasks('installBuildDependencies')
        }

        assert new File(resource, '.gogradle/build_gopath/src/localhost/a/.CURRENT_VERSION').exists()
        assert new File(resource, '.gogradle/build_gopath/src/localhost/b/.CURRENT_VERSION').exists()
        assert !new File(resource, '.gogradle/build_gopath/src/localhost/c/.CURRENT_VERSION').exists()

        IOUtils.write(resource, '.gogradle/build_gopath/src/localhost/b/IT_WILL_STAY', '')
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='github.com/my/project'
}

repositories {
    golang {
        root ~/localhost.\\w/
        url {it.replace('localhost','http://localhost:8080')}
    }
}

dependencies {
    golang {
        build 'localhost/b'
        build 'localhost/c'
    }
}
""")

        newBuild {
            it.forTasks('installBuildDependencies')
        }

        assert !new File(resource, '.gogradle/build_gopath/src/localhost/a/.CURRENT_VERSION').exists()
        assert new File(resource, '.gogradle/build_gopath/src/localhost/b/.CURRENT_VERSION').exists()
        assert new File(resource, '.gogradle/build_gopath/src/localhost/c/.CURRENT_VERSION').exists()
        assert new File(resource, '.gogradle/build_gopath/src/localhost/b/IT_WILL_STAY').exists()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
