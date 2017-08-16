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

import com.github.blindpirate.gogradle.support.*
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithIsolatedUserhome
@WithMockGo
class UnrecognizedPackageIntegrationTest extends IntegrationTestSupport {
    @Before
    void setUp() {
        IOUtils.mkdir(resource, 'project')
        IOUtils.mkdir(resource, 'unrecognized1')
        IOUtils.mkdir(resource, 'unrecognized2')

        IOUtils.write(resource, 'unrecognized1/main.go', '''
package main
import (
"unrecognized2"
"unrecognized2/sub"
)
''')
        IOUtils.write(resource, 'unrecognized2/main.go', '''
package main
import (
"unrecognized3"
"unrecognized3/sub"
"unrecognized3/sub/sub"
)
''')
    }

    String getResourceDir(String dirName) {
        return StringUtils.toUnixString(new File(resource, dirName))
    }

    @Test
    void 'build should succeed if unrecognized package is excluded'() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='my/project'
}
dependencies {
    golang {
        build (name:'unrecognized1', dir: '${getResourceDir("unrecognized1")}'){
            exclude name:'unrecognized2'
       }
    }
}
""")
        newBuild {
            it.forTasks('resolveBuildDependencies')
        }

    }

    @Test
    @WithGitRepo(repoName = 'helloworld', fileName = 'helloworld.go')
    void 'build should succeed if url of unrecognized package is provided as repository'() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='my/project'
}

repositories {
    golang {
        root 'unrecognized2'
        url 'http://localhost:8080/helloworld'
    }
}

dependencies {
    golang {
        build (name:'unrecognized1', dir: '${getResourceDir("unrecognized1")}') 
    }
}

""")
        newBuild {
            it.forTasks('vendor')
        }
        assert new File(resource, 'project/vendor/unrecognized2/helloworld.go').exists()
    }

    // https://github.com/gogradle/gogradle/issues/141
    @Test
    @WithGitRepo(repoName = 'helloworld', fileName = 'helloworld.go')
    void 'build should succeed if url of unrecognized package is provided directly'() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='my/project'
}

dependencies {
    golang {
        build (name:'unrecognized1', dir: '${getResourceDir("unrecognized1")}') 
        build (name:'unrecognized2', url: 'http://localhost:8080/helloworld') 
    }
}

""")
        newBuild {
            it.forTasks('vendor')
        }
        assert new File(resource, 'project/vendor/unrecognized2/helloworld.go').exists()
    }

    @Test
    void 'build should succeed if unrecognized package is provided as dir directly'() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='my/project'
}

dependencies {
    golang {
        build (name:'unrecognized1', dir: '${getResourceDir("unrecognized1")}'){
            exclude name:'unrecognized3'
        }
        build (name:'unrecognized2', dir:'${getResourceDir("unrecognized2")}'){
            exclude name:'unrecognized3'
        }
    }
}

""")
        newBuild {
            it.forTasks('resolveBuildDependencies')
        }
    }

    @Test
    void 'build should succeed if unrecognized package is provided as repository dir'() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='my/project'
}

repositories {
    golang {
        root 'unrecognized2'
        dir '${getResourceDir("unrecognized2")}'
    }
}

dependencies {
    golang {
        build (name:'unrecognized1', dir: '${getResourceDir("unrecognized1")}'){
            exclude name:'unrecognized3'
        }
    }
}

""")
        newBuild {
            it.forTasks('resolveBuildDependencies')
        }
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'project')
    }

}
