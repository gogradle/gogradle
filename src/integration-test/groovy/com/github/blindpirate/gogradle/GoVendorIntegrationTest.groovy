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
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
class GoVendorIntegrationTest extends IntegrationTestSupport {
    /*
    vendor/github.com/user/a
      |- a.go
      \- vendor/github.com/user/b
          \- b.go
     */

    @Before
    void setUp() {
        IOUtils.write(resource, 'vendor/github.com/user/a/a.go', '')
        IOUtils.write(resource, 'vendor/github.com/user/a/vendor/github.com/user/b/b.go', '')
        IOUtils.write(resource, 'vendor/vendor.json', '')

        IOUtils.write(resource, '.tmp/b1/b1.go', '')
        IOUtils.write(resource, '.tmp/b1/vendor/github.com/user/a/a1.go', '')
        IOUtils.write(resource, '.tmp/b1/vendor/github.com/user/a/vendor/github.com/user/c/c1.go', '')
        IOUtils.write(resource, '.tmp/b2/b2.go', '')
        IOUtils.write(resource, '.tmp/d/d.go', '')

        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath = 'github.com/my/package'
}
dependencies {
    golang {
        build name:'github.com/user/b',dir:'${StringUtils.toUnixString(new File(resource, '.tmp/b1'))}'
        build name:'github.com/user/d',dir:'${StringUtils.toUnixString(new File(resource, '.tmp/d'))}'
        test name:'github.com/user/b',dir:'${StringUtils.toUnixString(new File(resource, '.tmp/b2'))}'
    }
}
""")
    }

    @Test
    void 'vendor task should succeed'() {
        newBuild {
            it.forTasks('vendor')
        }

        assert new File(resource, 'vendor/github.com/user/a/a1.go').exists()
        assert !new File(resource, 'vendor/github.com/user/a/vendor').exists()
        assert new File(resource, 'vendor/github.com/user/b/b1.go').exists()
        assert !new File(resource, 'vendor/github.com/user/b/vendor').exists()
        assert new File(resource, 'vendor/github.com/user/c/c1.go').exists()
        assert new File(resource, 'vendor/github.com/user/d/d.go').exists()
        assert !new File(resource, 'vendor/vendor.json').exists()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
