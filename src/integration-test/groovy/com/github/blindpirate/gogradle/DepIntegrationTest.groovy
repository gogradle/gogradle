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
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@OnlyWhen("System.getenv('DEP_DIR')!=null&&'git version'.execute()")
class DepIntegrationTest extends IntegrationTestSupport {
    @Override
    File getProjectRoot() {
        return new File(System.getenv('DEP_DIR'))
    }

    String buildDotGradle = """
buildscript {
    dependencies {
        classpath files("${System.getProperty('GOGRADLE_ROOT')}/build/libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar")
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath="github.com/golang/dep"
    goVersion='1.9'
}
"""

    @Test
    void 'building dep should succeed'() {
        IOUtils.write(getProjectRoot(), 'build.gradle', buildDotGradle)
        init()
        try {
            newBuild {
                it.forTasks('clean', 'build', 'check', 'lock')
            }
        } finally {
            println(stdout)
            println(stderr)
        }
    }
}
