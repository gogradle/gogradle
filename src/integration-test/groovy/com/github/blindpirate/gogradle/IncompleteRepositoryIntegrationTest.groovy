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
import com.github.blindpirate.gogradle.support.WithIsolatedUserhome
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithMockGo
@WithIsolatedUserhome
@WithResource('')
class IncompleteRepositoryIntegrationTest extends IntegrationTestSupport {
    @Before
    void setUp() {
        new File(resource, 'dep').mkdir()
        IOUtils.mkdir(resource, 'project')
        IOUtils.write(resource, 'dep/vendor/a/a.go', '')
        IOUtils.write(resource, 'dep/vendor/a/b/b.go', '')
        IOUtils.write(resource, 'dep/vendor/a/b/c/c.go', '')
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='github.com/my/project'
}

repositories {
    golang {
        incomplete 'a'
    }

    golang {
        root 'a/b'
        url 'url'
    }
}

dependencies {
    golang {
        build name:'github.com/my/dep', dir:'${StringUtils.toUnixString(new File(resource, 'dep'))}'
    }
}
""")
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'project')
    }

    @Test
    void 'sub dependencies should start with b'() {
        newBuild {
            it.forTasks('dependencies')
        }

        assert stdout.toString().contains('a/b:')
    }
}
