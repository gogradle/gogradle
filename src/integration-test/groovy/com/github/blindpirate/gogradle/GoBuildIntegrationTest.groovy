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
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path


@RunWith(GogradleRunner)
@WithResource('')
@AccessWeb
class GoBuildIntegrationTest extends IntegrationTestSupport {

    ProcessUtils processUtils = new ProcessUtils()

    String buildDotGradle
    String settingDotGradle
    String subMainDotGo
    String printDotGo
    String mainDotGo
    String mainWithBuildTagDotGo

    @Before
    void setUp() {
        buildDotGradle = """
            ${buildDotGradleBase}
            golang {
                packagePath='github.com/my/package'
                goVersion='1.8'
            }
        """
        settingDotGradle = 'rootProject.name="myPackage"'
        mainDotGo = '''
// +build !a,!b

package main

import "github.com/my/package/print"

func main(){
    print.PrintHello() 
}
'''
        mainWithBuildTagDotGo = '''
// +build a b

package main

import "github.com/my/package/print"

func main(){
    print.PrintHello()
    print.PrintWorld()
}
'''
        printDotGo = '''
package print 

import "fmt"

func PrintHello(){
    fmt.Printf("Hello")
}
func PrintWorld(){
    fmt.Printf("World")
}
'''
        subMainDotGo = '''
package main
import "github.com/my/package/print"

func main(){
    print.PrintWorld() 
}
'''

        IOUtils.write(resource, 'main.go', mainDotGo)
        IOUtils.write(resource, 'mainWithBuildTag.go', mainWithBuildTagDotGo)
        IOUtils.write(resource, 'print/print.go', printDotGo)
        IOUtils.write(resource, 'sub/main.go', subMainDotGo)
        writeBuildAndSettingsDotGradle(buildDotGradle, settingDotGradle)
    }

    @Test
    void 'build should succeed'() {
        appendOnBuildDotGradle('''
            project.version = 1.0
            build {
                outputLocation = './.gogradle/${PROJECT_NAME}-${PROJECT_VERSION}-${GOOS}-${GOARCH}'
            }
            ''')
        appendOnBuildDotGradle("""
            build {
                targetPlatform = 'darwin-amd64, windows-amd64, linux-386, ${Os.getHostOs()}-${Arch.getHostArch()}'
            }
            """)

        newBuild('build')

        assert !buildUpToDate()

        ["myPackage-1.0-darwin-amd64", 'myPackage-1.0-windows-amd64', 'myPackage-1.0-linux-386'].each {
            assert new File(resource, ".gogradle/${it}").exists()
        }
        assert runExecutable(".gogradle/myPackage-1.0-${Os.getHostOs()}-${Arch.getHostArch()}") == 'Hello'

        ["myPackage-1.0-darwin-amd64", 'myPackage-1.0-windows-amd64', 'myPackage-1.0-linux-386'].each {
            assert new File(resource, ".gogradle/${it}").delete()
        }

        newBuild('build', '-i')

        assert !stdout.toString().contains(':buildDarwinAmd64 UP-TO-DATE')
        assert !stdout.toString().contains(':buildWindowsAmd64 UP-TO-DATE')
        assert !stdout.toString().contains(':buildLinux386 UP-TO-DATE')
    }

    @Test
    void 'build with build tags should succeed'() {
        appendOnBuildDotGradle('''
            golang {
                buildTags = ['a']
            }
        ''')

        newBuild('build')

        assert runExecutable(".gogradle/myPackage-${Os.getHostOs()}-${Arch.getHostArch()}") == 'HelloWorld'
    }

    void appendOnBuildDotGradle(String s) {
        IOUtils.append(new File(resource, 'build.gradle'), s)
    }

    private String runExecutable(String name) {
        Path exe = resource.toPath().resolve(name)
        if (Os.getHostOs() == Os.WINDOWS) {
            exe.renameTo(exe.toString() + '.exe')
        }
        IOUtils.chmodAddX(exe)

        return processUtils.getResult(processUtils.run(exe.toFile().absolutePath)).stdout
    }

    @Test
    void 'customized build should succeed'() {
        appendOnBuildDotGradle('''
            build {
                go 'build -o ${GOOS}_${GOARCH}_output github.com/my/package/sub'
            }
        ''')
        newBuild('build', '--info')

        buildActionShouldExecuteOnlyOnce(stdout.toString())
        assert runExecutable("${Os.getHostOs()}_${Arch.getHostArch()}_output") == 'World'
    }

    def buildActionShouldExecuteOnlyOnce(String infoLog) {
        List<String> lines = StringUtils.splitAndTrim(infoLog, /\n/)

        // :buildDarwinAmd64 (Thread[Task worker for ':',5,main]) completed. Took 0.288 secs.
        // :build (Thread[Task worker for ':',5,main]) completed. Took 0.253 secs.)
        List<Double> buildTimes = lines.collect {
            if (it ==~ /^:build.*?Took ([\d.]+) secs.$/) {
                return (it =~ /^:build.*?Took ([\d.]+) secs.$/)[0][1].toDouble()
            } else {
                return null
            }
        }.grep(Double)
        assert buildTimes.size() == 2
        // empty build task should take no more than 100ms
        assert buildTimes[1] < 0.1
    }

    @Test
    void 'can run multiple go command'() {
        // given
        appendOnBuildDotGradle('''
            build {
                go 'build -o ./out/main github.com/my/package'
                go 'build -o ./out/sub github.com/my/package'
            }
        ''')
        // when
        newBuild('build')
        // then
        assert new File(resource, 'out/main').exists()
        assert new File(resource, 'out/sub').exists()
    }

    boolean buildUpToDate() {
        return stdout.toString().readLines().any { it ==~ /^:build\w+ UP-TO-DATE$/ }
    }

    void firstBuild() {
        // when
        newBuild('build', '--console=plain')
        // then
        assert !buildUpToDate()
    }

    @Test
    void 'no incremental build if outputLocation is not set'() {
        // given
        appendOnBuildDotGradle('''
            build {
                go 'build -o ./${PROJECT_NAME}-${GOOS}-${GOARCH} github.com/my/package'
            }
        ''')

        // when
        firstBuild()
        // then
        newBuild('build', '--console=plain')
        assert !buildUpToDate()
    }

    @Test
    void 'incremental build by default'() {
        // when
        firstBuild()
        // then
        newBuild('build', '--console=plain')
        assert buildUpToDate()
    }

    @Test
    void 'modification to build tags causes build out-of-date'() {
        // given
        'incremental build by default'()
        // when
        appendOnBuildDotGradle('''
            golang {
                buildTags = ['abc']
            }
        ''')
        // then
        newBuild('build', '--console=plain')
        assert !buildUpToDate()
    }

    @Test
    void 'modification to goVersion causes build out-of-date'() {
        // given
        'incremental build by default'()
        // when
        appendOnBuildDotGradle('''
            golang {
                goVersion = '1.9'
            }
        ''')
        // then
        newBuild('build', '--console=plain')
        assert !buildUpToDate()
    }

    @Test
    void 'modification to environment cause build out-of-date'() {
        // given
        'incremental build by default'()
        // when
        appendOnBuildDotGradle('''
            build {
                environment 'a','b'
            }
        ''')
        // then
        newBuild('build', '--console=plain')
        assert !buildUpToDate()
    }

    @Test
    void 'modification to build go files cause build out-of-date'() {
        // given
        'incremental build by default'()
        // when
        new File(resource, 'main.go') << '\n\n'

        // then
        newBuild('build', '--console=plain')
        assert !buildUpToDate()
    }

    @Test
    void 'modification to vendor go files cause build out-of-date'() {
        // given
        prepareAMockDependency()
        'incremental build by default'()
        // when
        IOUtils.deleteQuitely(new File(resource, '.dep/dep.go'))

        // then
        newBuild('build', '--console=plain')
        assert !buildUpToDate()
    }

    def prepareAMockDependency() {
        IOUtils.write(resource, '.dep/dep.go', '')
        IOUtils.write(resource, '.dep/dep_test.go', '')
        appendOnBuildDotGradle("""
            dependencies {
                golang {
                    build name:'local/dep', dir:'${StringUtils.toUnixString(new File(resource, '.dep'))}'
                }
            }
        """)
    }

    @Test
    void 'modification to test go files does not cause build out-of-date'() {
        // given
        IOUtils.write(resource, 'main_test.go', '''
        package main

        import "testing"

        func Test_main(t *testing.T){
        }
        ''')
        'incremental build by default'()
        // when
        new File(resource, 'main_test.go') << '\n\n'

        // then
        newBuild('build', '--console=plain')
        assert buildUpToDate()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
