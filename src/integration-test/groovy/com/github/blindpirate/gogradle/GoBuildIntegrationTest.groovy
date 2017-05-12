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
        settingDotGradle = '''
rootProject.name="myPackage"
'''
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
        appendOnBuildDotGradle("""
goBuild {
    targetPlatform = 'darwin-amd64, windows-amd64, linux-386, ${Os.getHostOs()}-${Arch.getHostArch()}'
}
""")

        newBuild {
            it.forTasks('goBuild')
        }

        ["darwin_amd64_myPackage", 'windows_amd64_myPackage', 'linux_386_myPackage'].each {
            assert new File(resource, ".gogradle/${it}").exists()
        }

        assert runExecutable(".gogradle/${Os.getHostOs()}_${Arch.getHostArch()}_myPackage") == 'Hello'
    }

    @Test
    void 'build with build tags should succeed'() {
        appendOnBuildDotGradle('''
golang {
    buildTags = ['a']
}
''')

        newBuild {
            it.forTasks('goBuild')
        }

        assert runExecutable(".gogradle/${Os.getHostOs()}_${Arch.getHostArch()}_myPackage") == 'HelloWorld'
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
goBuild {
    doLast {
        go 'build -o ${GOOS}_${GOARCH}_output github.com/my/package/sub'
    }
}
''')
        newBuild {
            it.forTasks('goBuild')
        }

        assert runExecutable("${Os.getHostOs()}_${Arch.getHostArch()}_output") == 'World'
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
