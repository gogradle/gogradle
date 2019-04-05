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

import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@WithResource('')
@RunWith(GogradleRunner)
class MultiProjectIntegrationTest extends IntegrationTestSupport {
    @Override
    File getProjectRoot() {
        return resource
    }

    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle("""
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}

apply plugin: 'java'

allprojects {
    if(it.name.startsWith('go')){
        it.apply plugin:'com.github.blindpirate.gogradle'
        it.dependencies {
            golang {
                build name:'my/common', dir: new File(rootDir, 'go-common') 
            }
        }
        
        def projectName = it.name
        it.golang {
            packagePath = projectName
        }
    }
}

build.dependsOn ':go1:goBuild'
build.dependsOn ':go2:goBuild'
""", "include 'go1', 'go2'\nrootProject.name='multi'")

        IOUtils.write(resource, 'src/main/java/a/A.java', '''
package a;
class A{}
''')

        IOUtils.write(resource, 'go-common/common.go', '''
package common
import "fmt" 
func Say(s string){
    fmt.Println(s)
}
''')
        IOUtils.write(resource, 'go1/src/go1/main.go', '''
package go1
import "my/common"
func main(){
    common.Say("go1")
}
''')
        IOUtils.write(resource, 'go2/src/go2/main.go', '''
package go2
import "my/common"
func main(){
    common.Say("go2")
}
''')
    }

    @Test
    void 'root project is java and sub is go'() {
        newBuild('build', '--parallel')

        assert new File(resource, "go1/.gogradle/go1-${Os.getHostOs()}-${Arch.getHostArch()}").exists()
        assert new File(resource, "go2/.gogradle/go2-${Os.getHostOs()}-${Arch.getHostArch()}").exists()
        assert new File(resource, "build/libs/multi.jar").exists()
    }

    @Test
    void 'each project should have a different injector instance'() {
        // given
        new File(resource, 'build.gradle').append('''
allprojects {
    if(it.name.startsWith('go')){
        it.task(type: com.github.blindpirate.gogradle.Go, 'printInjector') {
            doLast {
                println "injector:${-> com.github.blindpirate.gogradle.GogradleGlobal.INSTANCE.getInjector().hashCode()}"
            }
        }
    }
}
''')
        // when
        newBuild {
            it.forTasks(':go1:printInjector', ':go2:printInjector')
        }

        // then
        def matchers = (stdout.toString() =~ /injector:(\d+)/)
        assert matchers.size() == 2
        assert matchers[0][1] != matchers[1][1]
    }
}
