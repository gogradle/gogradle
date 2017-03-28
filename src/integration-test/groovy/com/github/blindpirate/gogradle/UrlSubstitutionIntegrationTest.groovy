package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class UrlSubstitutionIntegrationTest extends IntegrationTestSupport {

    @Override
    File getProjectRoot() {
        return resource
    }

    String buildDotGradle = """
${buildDotGradleBase}

golang {
    packagePath='my/project'
}

repositories {
    golang {
        all()
        url {
            'https://github.com/blindpirate/test-for-gogradle.git'
        }
    }
}

dependencies {
    build 'my/awesome/project'
}
"""

    @Test
    @AccessWeb
    void 'url substitution should succeed'() {
        IOUtils.write(resource, 'build.gradle', buildDotGradle)
        try {
            newBuild {
                it.forTasks('resolveBuildDependencies')
            }
        } finally {
            println(stderr)
            println(stdout)
        }

        assert new File(resource, ".gogradle/build_gopath/src/my/awesome/project/helloworld.go").exists()
    }
}
