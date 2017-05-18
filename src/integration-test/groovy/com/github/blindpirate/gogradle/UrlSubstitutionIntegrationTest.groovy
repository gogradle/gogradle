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
    golang {
        build 'my/awesome/project'
    }
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
        newBuild {
            it.forTasks('goVendor')
        }

        assert new File(resource, "vendor/my/awesome/project/main.go").exists()
    }
}
