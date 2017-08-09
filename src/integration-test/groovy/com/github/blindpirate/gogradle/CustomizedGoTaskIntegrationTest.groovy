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

import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class CustomizedGoTaskIntegrationTest extends IntegrationTestSupport {
    String lsCmd = Os.getHostOs() == Os.WINDOWS ? 'cmd /C dir' : 'ls'

    @Before
    void setUp() {
        String buildDotGradle = """
${buildDotGradleBase}

golang {
    packagePath='a/b/c'
}

task version(type: com.github.blindpirate.gogradle.Go){
    go 'version'
}

task ls(type: com.github.blindpirate.gogradle.Go){
    dependsOn 'version'
    run '${lsCmd}'
}
"""
        writeBuildAndSettingsDotGradle(buildDotGradle)
    }

    @Test
    void 'customized go task should succeed'() {
        newBuild {
            it.forTasks('ls')
        }
        assert stdout.toString().contains('go version')
        assert stdout.toString().contains('settings.gradle')
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
