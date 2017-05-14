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
class VendorTaskIntegrationTest extends IntegrationTestSupport {
    /*
    vendor/github.com/user/a
      |- a.go
      \- vendor/github.com/user/b
          |- b.go
          \- vendor/github.com/user/c
             \- c.go
     */

    @Before
    void setUp() {
        IOUtils.write(resource, 'vendor/github.com/user/a/a.go', '')
        IOUtils.write(resource, 'vendor/github.com/user/a/vendor/github.com/user/b/b.go', '')
        IOUtils.write(resource,
                'vendor/github.com/user/a/vendor/github.com/user/b/vendor/github.com/user/c/c.go', '')
        IOUtils.write(resource, 'vendor/vendor.json', '')

        IOUtils.write(resource, '.tmp/b1/b1.go', '')
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
            it.setJvmArguments('-Dgogradle.mode=DEVELOP')
            it.forTasks('goVendor')
        }

        assert !new File(resource, 'vendor/github.com/user/a/vendor').exists()
        assert new File(resource, 'vendor/github.com/user/a/a.go').exists()
        assert new File(resource, 'vendor/github.com/user/b/b1.go').exists()
        assert !new File(resource, 'vendor/github.com/user/b/vendor').exists()
        assert !new File(resource, 'vendor/github.com/user/c/c.go').exists()
        assert new File(resource, 'vendor/github.com/user/d/d.go').exists()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
