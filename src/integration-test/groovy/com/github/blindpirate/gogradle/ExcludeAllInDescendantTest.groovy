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
            it.forTasks('goDependencies', 'goVendor')
        }

        assert new File(resource, 'project/vendor/a/a.go').exists()
        assert new File(resource, 'project/vendor/b/b.go').exists()
        assert new File(resource, 'project/vendor/c/c.go').exists()
        assert new File(resource, 'project/vendor/d/d.go').exists()
        assert !new File(resource, 'project/vendor/e').exists()
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'project')
    }
}
