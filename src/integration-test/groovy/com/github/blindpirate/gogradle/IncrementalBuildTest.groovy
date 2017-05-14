/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

    void build(List arguments) {
        newBuild({
            it.forTasks('resolveBuildDependencies')
        }, arguments)
    }

    void build() {
        build([])
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
    golang {
        build name:'tmp', dir: '${StringUtils.toUnixString(new File(resource, '.tmp'))}'
    }
}
""")
        build()
        assertNotUpToDate()

        IOUtils.write(resource, 'build.gradle', buildDotGradle + """
dependencies {
    golang {
        build(name:'tmp', dir: '${StringUtils.toUnixString(new File(resource, '.tmp'))}'){
            exclude name:'xxx'
        }
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

    @Test
    void 'up-to-date check should be always disabled if --refresh-dependencies exist'() {
        build(['--refresh-dependencies'])

        build(['--refresh-dependencies'])
        assertNotUpToDate()
        build(['--refresh-dependencies'])
        assertNotUpToDate()
    }

    @Test
    void 'modification to build mode should make dependencies updated'() {
        build()
        IOUtils.write(resource, 'build.gradle', buildDotGradle + 'golang {buildMode="DEVELOP"}')
        build()
        assertNotUpToDate()
    }

    @Test
    void 'modification to local dependencies should make dependencies updated'() {
        File originalResource = resource

        IOUtils.write(resource, 'a/a.go', '')
        IOUtils.write(resource, 'b/b.go', '')
        IOUtils.write(resource, 'project/build.gradle', """
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
golang {
    packagePath='my/project'
}
dependencies {
    golang {
        build name:'a', dir: '${StringUtils.toUnixString(new File(resource, 'a'))}'
        build name:'b', dir: '${StringUtils.toUnixString(new File(resource, 'b'))}'
    }
}
""")
        resource = new File(resource, 'project')
        build()
        build()
        assertUpToDate()
        IOUtils.write(originalResource, 'a/a.go', 'something else')
        build()
        assertNotUpToDate()
        resource = originalResource
    }
}
