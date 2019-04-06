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
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.apache.commons.io.FilenameUtils
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@RunWith(GogradleRunner)
@OnlyWhen("System.getenv('GOGS_DIR')!=null&&'git version'.execute()")
class GogsBuild extends IntegrationTestSupport {
    File resource = new File(System.getenv('GOGS_DIR'))


    def packageName = 'github.com/gogits/gogs'

    def createSymLink() {
        Path link = Paths.get(resource.getAbsolutePath(), "src", packageName)
        if (!link.getParent().toFile().exists()){
            IOUtils.forceMkdir(link.getParent().toFile())
        }
        if (!link.toFile().exists()) {
            Files.createSymbolicLink(link, resource.toPath())
        }
    }

    ProcessUtils processUtils = new ProcessUtils()

    String buildDotGradle = """
buildscript {
    dependencies {
        classpath files("${System.getProperty('GOGRADLE_ROOT')}/build/libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar")
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath="${packageName}"
    goVersion='1.8'
}

goVet {
    continueOnFailure = true
}

"""

    @Test
    @AccessWeb
    void 'gogs should be built successfully'() {
        // v0.11
        assert processUtils.run(['git', 'checkout', '348c75c91b95ce7fb0f6dac263aa7290f2319e1b', '-f'], null, resource).waitFor() == 0


        createSymLink()
        // I don't know why it will fail on Windows
        if (Os.getHostOs() == Os.WINDOWS) {
            writeBuildAndSettingsDotGradle(buildDotGradle + 'goTest.enabled = false\n')
        } else {
            writeBuildAndSettingsDotGradle(buildDotGradle)
        }

        init()

        addMissingDependencies()

        firstBuild()

        File firstBuildResult = getOutputExecutable()
//        long lastModified = firstBuildResult.lastModified()
//        String md5 = DigestUtils.md5Hex(new FileInputStream(firstBuildResult))
        assert processUtils.getStdout(processUtils.run(firstBuildResult.absolutePath)).contains('Gogs')
        assert new File(resource, 'gogradle.lock').exists()

//        secondBuild()
//
//        File secondBuildResult = getOutputExecutable()
//        assert processUtils.getStdout(processUtils.run(secondBuildResult.absolutePath)).contains('Gogs')
//        assert secondBuildResult.lastModified() > lastModified
//        assert DigestUtils.md5Hex(new FileInputStream(secondBuildResult)) == md5
    }

    void addMissingDependencies() {
        // I don't know why this is missing in vendor.json
        IOUtils.append(new File(resource, 'build.gradle'), '''
dependencies {
    golang {
        build 'gopkg.in/bufio.v1'
    }
}
''')
    }

    void firstBuild() {
        newBuild {
            it.forTasks('goClean', 'goBuild', 'goCheck', 'goLock')
        }
    }

    void secondBuild() {
        newBuild {
            it.forTasks('goBuild', 'goCheck')
        }
    }

    File getOutputExecutable() {
        Path gogsBinPath = resource.toPath().resolve(".gogradle/gogs-${Os.getHostOs()}-${Arch.getHostArch()}")
        if (Os.getHostOs() == Os.WINDOWS) {
            gogsBinPath.renameTo(gogsBinPath.toString() + '.exe')
        }
        IOUtils.chmodAddX(gogsBinPath)
        return gogsBinPath.toFile()
    }


    @Override
    File getProjectRoot() {
        return resource
    }
}
