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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
class GoInitIntegrationTest extends IntegrationTestSupport {

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
golang {
    packagePath='github.com/my/package'
}
""")
    }

    @Test
    void 'initialization of glide project should succeed'() {
        IOUtils.write(resource, 'glide.lock', '''
hash: 1d5fcf2a90f7621ecbc0b1abed548e11d13bda3fea49b4326c829a523268e5cf
updated: 2016-06-12T17:35:14.27036884+08:00
imports:
- name: github.com/bradfitz/gomemcache
  version: 2fafb84a66c4911e11a8f50955b01e74fe3ab9c5
  subpackages:
  - memcache
- name: github.com/urfave/cli
  version: 0bdeddeeb0f650497d603c4ad7b20cfe685682f6
testImports:
- name: github.com/go-macaron/cache
  version: 56173531277692bc2925924d51fda1cd0a6b8178
  subpackages:
  - memcache
  - redis
''')
        newBuild {
            it.forTasks('init')
        }

        assert new File(resource, 'build.gradle').text.contains('''\
dependencies {
    golang {
        build name:'github.com/bradfitz/gomemcache', version:'2fafb84a66c4911e11a8f50955b01e74fe3ab9c5', subpackages:[".","memcache"], transitive:false
        build name:'github.com/urfave/cli', version:'0bdeddeeb0f650497d603c4ad7b20cfe685682f6', transitive:false
        test name:'github.com/go-macaron/cache', version:'56173531277692bc2925924d51fda1cd0a6b8178', subpackages:[".","redis","memcache"], transitive:false
    }
}''')
        newBuild {
            it.forTasks('init')
        }

        assert stdout.toString().contains('This project seems to have been initialized already, skip')
    }

    @Test
    void 'initialization of project with pure source should succeed'() {
        IOUtils.write(resource, 'main.go', '''
package main
import "github.com/a/b"
''')
        IOUtils.write(resource, 'main_test.go', '''
package main
import "github.com/c/d"
''')
        newBuild {
            it.forTasks('init')
        }

        assert new File(resource, 'build.gradle').text.contains('''\
dependencies {
    golang {
        build name:'github.com/a/b'
        test name:'github.com/c/d'
    }
}''')
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
