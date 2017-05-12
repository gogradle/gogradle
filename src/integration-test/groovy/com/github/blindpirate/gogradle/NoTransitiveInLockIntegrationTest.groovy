package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.lock.GogradleLockModel
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString

@RunWith(GogradleRunner)
@WithResource('')
@WithMockGo
class NoTransitiveInLockIntegrationTest extends IntegrationTestSupport {
    File resource

    @Before
    void setUp() {
        GogradleLockModel bModel = GogradleLockModel.of([[name: 'c', 'dir': toUnixString(new File(resource, 'c'))]], [])
        IOUtils.write(new File(resource, 'b/gogradle.lock'), DataExchange.toYaml(bModel))

        IOUtils.write(new File(resource, 'c/c.go'), '')

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
        build name:'b', dir:'${toUnixString(new File(resource, 'b'))}'
    }
}
""")
    }

    @Test
    void 'transitive:false should be added to gogradle.lock'() {
        newBuild {
            it.forTasks('goDependencies', 'goLock')
        }
        assert stdout.toString().contains("""\
a
\\-- b:${toUnixString(resource)}/b
    \\-- c:${toUnixString(resource)}/c
""")

        // at second time, all dependencies should be flattened
        newBuild {
            it.forTasks('goDependencies')
        }
        assert stdout.toString().contains("""\
a
|-- b:${toUnixString(resource)}/b
\\-- c:${toUnixString(resource)}/c
""")
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'a')
    }
}
