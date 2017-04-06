package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class IncrementalBuildTest extends IntegrationTestSupport {

    @Override
    File getProjectRoot() {
        return resource
    }

    String buildDotGradle

    String gogradleDotLock =
            """---
apiVersion: \"${GogradleGlobal.GOGRADLE_VERSION}\"
dependencies:
  build: []
  test: []
"""

    @Before
    void setUp() {
        buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='my/project'
}
"""
        IOUtils.write(resource, 'gogradle.lock', gogradleDotLock)
        IOUtils.write(resource, 'a.go', '')
        IOUtils.write(resource, 'a_test.go', '')
        IOUtils.write(resource, 'vendor/a/b/c.go', '')
        IOUtils.write(resource, 'build.gradle', buildDotGradle)
        IOUtils.write(resource, '.hidden/a.go', '')
        IOUtils.write(resource, '_hidden/a.go', '')
        IOUtils.write(resource, '.a.go', '')
        IOUtils.write(resource, '_a.go', '')
        IOUtils.write(resource, 'sub/testdata/a.go', '')
    }

    @Test
    void 'second build should be UP-TO-DATE'() {
        build()
        build()
        assertUpToDate()
    }

    void assertUpToDate() {
        assert stdout.toString().contains(':resolveBuildDependencies UP-TO-DATE')
    }

    void build() {
        initStdoutStderr()
        newBuild {
            it.forTasks('resolveBuildDependencies')
        }
    }

    @Test
    void 'modification to external lock file should make dependencies updated'() {
        build()
        IOUtils.write(resource, 'gogradle.lock', gogradleDotLock + '\n')
        build()
        assertNotUpToDate()
    }

    void assertNotUpToDate() {
        assert !stdout.toString().contains(':resolveBuildDependencies UP-TO-DATE')
    }

    @Test
    void 'modification to normal go files should make dependencies updated'() {
        build()
        IOUtils.write(resource, 'a.go', 'modified')
        build()
        assertNotUpToDate()
    }

    @Test
    void 'modification to vendor should make dependencies updated'() {
        build()
        IOUtils.write(resource, 'vendor/a/b/c.go', 'modified')
        build()
        assertNotUpToDate()
    }

    @Test
    void 'modification to buildTags should make dependencies updated'() {
        build()
        IOUtils.write(resource, 'build.gradle', buildDotGradle + """
golang{
    buildTags=['tag']
}
""")
        build()
        assertNotUpToDate()
    }

    @Test
    void 'modification to dependencies should make dependencies updated'() {
        build()
        IOUtils.mkdir(resource, '.tmp')
        IOUtils.write(resource, 'build.gradle', buildDotGradle + """
dependencies {
    build name:'tmp', dir: '${StringUtils.toUnixString(new File(resource, '.tmp'))}'
}
""")
        build()
        assertNotUpToDate()

        IOUtils.write(resource, 'build.gradle', buildDotGradle + """
dependencies {
    build(name:'tmp', dir: '${StringUtils.toUnixString(new File(resource, '.tmp'))}'){
        exclude name:'xxx'
    }
}
""")
        build()
        assertNotUpToDate()
    }

    @Test
    void 'modification to testdata/_/. go files should not make dependencies updated'() {
        build()
        IOUtils.write(resource, '.hidden/a.go', 'modified')
        IOUtils.write(resource, '_hidden/a.go', 'modified')
        IOUtils.write(resource, '.a.go', 'modified')
        IOUtils.write(resource, '_a.go', 'modified')
        IOUtils.write(resource, 'sub/testdata/a.go', 'modified')
        build()
        assertUpToDate()
    }
}
