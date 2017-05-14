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
        newBuild {
            it.forTasks('iBD', 'gD')
        }
        println(stderr)
        assert stdout.toString().contains("""\
a
\\-- c:${toUnixString(resource)}/c
    \\-- d:c@${toUnixString(resource)}/c/vendor/d
        \\-- e:c@${toUnixString(resource)}/c/vendor/d/vendor/e""")
    }

    @Override
    File getProjectRoot() {
        return new File(resource, 'a')
    }
}
