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
            it.forTasks('goVendor')
        }

        assert new File(resource, 'vendor/github.com/user/b/b.go').exists()
    }
}
