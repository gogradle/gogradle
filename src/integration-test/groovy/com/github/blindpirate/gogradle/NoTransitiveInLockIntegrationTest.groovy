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
    void 'transitive:false should be added to gogradle_lock'() {
        newBuild {
            it.forTasks('dependencies', 'lock')
        }
        assert stdout.toString().contains("""\
a
\\-- b:${toUnixString(resource)}/b
    \\-- c:${toUnixString(resource)}/c
""")

        // at second time, all dependencies should be flattened
        newBuild {
            it.forTasks('dependencies')
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
