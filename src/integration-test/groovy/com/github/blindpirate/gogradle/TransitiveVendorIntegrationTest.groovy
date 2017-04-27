package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.lock.GogradleLockModel
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.DataExchange.toYaml
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
class TransitiveVendorIntegrationTest extends IntegrationTestSupport {

    GogradleLockModel modelOf(Map... buildDependencies) {
        return GogradleLockModel.of(buildDependencies as List, [[:]])
    }

    @Before
    void setUp() {
        GogradleLockModel cModel = modelOf([name: 'd', host: [name: 'GOGRADLE_ROOT'], vendorPath: 'vendor/d'],
                [name: 'e', host: [name: 'GOGRADLE_ROOT'], vendorPath: 'vendor/d/vendor/e'])
        IOUtils.write(resource, 'c/gogradle.lock', toYaml(cModel))
        IOUtils.write(resource, 'c/vendor/d/d.go', '')
        IOUtils.write(resource, 'c/vendor/d/vendor/e/e.go', '')

        writeBuildAndSettingsDotGradle("""
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
golang {
    goExecutable = '${toUnixString(goBinPath)}'
    packagePath='a'
}

dependencies {
    golang {
        build name:'c', dir:'${toUnixString(new File(resource, 'c'))}'
    }
}
""")
    }

    @Test
    void 'resolving vendor with GOGRADLE_ROOT in transitive dependency should succeed'() {
        try {
            newBuild {
                it.forTasks('iBD', 'gD')
            }
        } finally {
            println(stderr)
            assert stdout.toString().contains("""\
a
\\-- c:${toUnixString(resource)}/c
    \\-- d:LocalDirectoryDependency@${toUnixString(resource)}/c/vendor/d
        \\-- e:LocalDirectoryDependency@${toUnixString(resource)}/c/vendor/d/vendor/e""")
        }
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'a')
    }
}
