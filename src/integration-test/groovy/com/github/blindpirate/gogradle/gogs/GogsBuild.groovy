package com.github.blindpirate.gogradle.gogs

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path

@RunWith(GogradleRunner)
@OnlyWhen("System.getenv('GOGS_DIR')!=null&&'git version'.execute()")
class GogsBuild extends IntegrationTestSupport {
    File resource = new File(System.getenv('GOGS_DIR'))

    ProcessUtils processUtils = new ProcessUtils()

    String buildDotGradle = """
buildscript {
    dependencies {
        classpath files("${System.getProperty('GOGRADLE_ROOT')}/build/libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar")
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath="github.com/gogits/gogs"
    goVersion='1.8'
}

vet {
    continueWhenFail = true
}

"""

    @Test
    @AccessWeb
    void 'gogs should be built successfully'() {
        // v0.9.113
        new ProcessUtils().run(['git', 'checkout', '114c179e5a50e3313f7a5894100693805e64e440'], null, resource)

        // I don't know why it will fail on Windows
        if (Os.getHostOs() == Os.WINDOWS) {
            writeBuildAndSettingsDotGradle(buildDotGradle + 'test.enabled = false')
        } else {
            writeBuildAndSettingsDotGradle(buildDotGradle)
        }

        try {
            newBuild {
                it.forTasks('build', 'check')
            }
        } catch (Exception e) {
            new File(resource, ".gogradle/reports/test/packages").listFiles().each {
                println(it.text)
            }
            new File(resource, ".gogradle/reports/test/classes").listFiles().each {
                println(it.text)
            }
            throw e
        } finally {
            println(stdout)
            println(stderr)
        }

        Path gogsBinPath = resource.toPath().resolve(".gogradle/${Os.getHostOs()}_${Arch.getHostArch()}_gogs")
        if (Os.getHostOs() == Os.WINDOWS) {
            gogsBinPath.renameTo(gogsBinPath.toString() + '.exe')
        }
        IOUtils.chmodAddX(gogsBinPath)

        Process process = processUtils.run(gogsBinPath.toFile().absolutePath)
        assert processUtils.getResult(process).stdout.contains('Gogs')

    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
