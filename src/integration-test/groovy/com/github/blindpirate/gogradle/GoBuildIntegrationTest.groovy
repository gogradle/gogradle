package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path


@RunWith(GogradleRunner)
@WithResource('')
class GoBuildIntegrationTest extends IntegrationTestSupport {

    File resource

    ProcessUtils processUtils = new ProcessUtils()

    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='github.com/my/package'
    targetPlatform = 'darwin-amd64, windows-amd64, linux-386, ${Os.getHostOs()}-${Arch.getHostArch()}'
}
"""
    String settingDotGradle = '''
rootProject.name="myPackage"
'''
    String mainDotGo = '''
package main

import "github.com/my/package/print"

func main(){
    print.PrintHello() 
}
'''
    String printDotGo = '''
package print 

import "fmt"

func PrintHello(){
    fmt.Printf("Hello")
}
func PrintWorld(){
    fmt.Printf("World")
}
'''
    String subMainDotGo = '''
package main
import "github.com/my/package/print"

func main(){
    print.PrintWorld() 
}
'''

    @Before
    void setUp() {
        IOUtils.write(resource, 'main.go', mainDotGo)
        IOUtils.write(resource, 'print/print.go', printDotGo)
        IOUtils.write(resource, 'sub/main.go', subMainDotGo)
        writeBuildAndSettingsDotGradle(buildDotGradle, settingDotGradle)
    }

    @Test
    void 'build should succeed'() {
        newBuild {
            it.forTasks('build')
        }

        ["darwin_amd64_myPackage", 'windows_amd64_myPackage', 'linux_386_myPackage'].each {
            assert new File(resource, ".gogradle/${it}").exists()
        }

        assert runExecutable(".gogradle/${Os.getHostOs()}_${Arch.getHostArch()}_myPackage") == 'Hello'

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
        writeBuildAndSettingsDotGradle(buildDotGradle + '''
build {
    doLast {
        go 'build -o ${GOOS}_${GOARCH}_output github.com/my/package/sub'
    }
}
''')
        try {
            newBuild {
                it.forTasks('build')
            }
        } catch (Exception e) {
            println(stderr)
            println(stdout)
        }

        ["darwin_amd64_output", 'windows_amd64_output', 'linux_386_output'].each {
            assert new File(resource, "${it}").exists()
        }

        assert runExecutable("${Os.getHostOs()}_${Arch.getHostArch()}_output") == 'World'
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
