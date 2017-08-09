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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
@OnlyWhen(value = '"golint".execute()', ignoreTestWhenException = OnlyWhen.ExceptionStrategy.TRUE)
class GolintIntegrationTest extends IntegrationTestSupport {
    String buildDotGradle
    String mainDotGo

    @Before
    void setUp() {
        buildDotGradle = """
${buildDotGradleBase}

golang {
    packagePath='a/b/c'
}

task golint(type: com.github.blindpirate.gogradle.Go){
    run 'golint a/b/c'
}
"""
        mainDotGo = '''
package main

import . "fmt"

func main(){
fmt.Printf("Hello")
}
'''
        writeBuildAndSettingsDotGradle(buildDotGradle)
        IOUtils.write(resource, 'main.go', mainDotGo)
    }

    @Test
    void 'golint should succeed'() {
        newBuild {
            it.forTasks('golint')
        }
        assert stdout.toString().contains('should not use dot imports')
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
