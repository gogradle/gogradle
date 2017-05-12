package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('exclude-descendants.zip')
// There should be a test with network access instead of vendor access
class ExcludeAllInDescendantTest extends IntegrationTestSupport {
    /*
        rootProject
            \-- a exclude e
                |-- b
                \-- c
                    \-- d
                        \-- e
 results:

        rootProject
            \-- a
                |-- b
                \-- c
                    \-- d
     */

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
golang {
    packagePath='my/project'
}
dependencies {
    golang {
        build (name:'a', dir: '${StringUtils.toUnixString(new File(resource, "a"))}'){
            exclude name:'e'
        }
    }
}
""")
    }

    @Test
    void 'exclusion in ancestor should succeed'() {
        newBuild {
            it.forTasks('goDependencies', 'installBuildDependencies')
        }

        assert new File(resource, 'project/.gogradle/build_gopath/src/a/a.go').exists()
        assert new File(resource, 'project/.gogradle/build_gopath/src/b/b.go').exists()
        assert new File(resource, 'project/.gogradle/build_gopath/src/c/c.go').exists()
        assert new File(resource, 'project/.gogradle/build_gopath/src/d/d.go').exists()
        assert !new File(resource, 'project/.gogradle/build_gopath/src/e').exists()
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'project')
    }
}
