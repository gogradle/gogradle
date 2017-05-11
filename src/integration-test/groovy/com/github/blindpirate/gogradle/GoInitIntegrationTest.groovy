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
            it.forTasks('goInit')
        }

        assert IOUtils.toString(new File(resource, 'build.gradle')).contains('''\
dependencies {
    golang {
        build name:'github.com/bradfitz/gomemcache', version:'2fafb84a66c4911e11a8f50955b01e74fe3ab9c5', transitive:false
        build name:'github.com/urfave/cli', version:'0bdeddeeb0f650497d603c4ad7b20cfe685682f6', transitive:false
        test name:'github.com/go-macaron/cache', version:'56173531277692bc2925924d51fda1cd0a6b8178', transitive:false
    }
}''')
        newBuild {
            it.forTasks('goInit')
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
            it.forTasks('goInit')
        }

        assert IOUtils.toString(new File(resource, 'build.gradle')).contains('''\
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
