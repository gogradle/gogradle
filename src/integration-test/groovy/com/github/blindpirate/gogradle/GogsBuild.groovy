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
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@WithResource('')
@RunWith(GogradleRunner)
@OnlyWhen("System.getenv('GOGS_DIR')!=null")
class GogsBuild extends IntegrationTestSupport {
//    = new File()

File resource =new File("/tmp/gogsbuild")
    def packageName = 'github.com/gogs/gogs'
    def outputLocation

    def createSymLink() {
        Path link = Paths.get(resource.getAbsolutePath(), "src", packageName)
        Path targetDir = Paths.get(System.getenv('GOGS_DIR'))
        if (!link.getParent().toFile().exists()) {
            IOUtils.forceMkdir(link.getParent().toFile())
            Files.createSymbolicLink(link, targetDir)
        }
    }

    ProcessUtils processUtils = new ProcessUtils()

    @Before
    void setUp() {
         outputLocation="${resource.toPath().toAbsolutePath().toString()}/build/bin/gogs"
//        outputLocation="/tmp/build/bin/gogs"
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}

golang {
    packagePath="${packageName}"
    mainApplicationPath="${packageName}"
}
goBuild {
    outputLocation="${outputLocation}"
}
goVet {
    continueOnFailure = true
}
dependencies {
    golang {
        build 'gopkg.in/bufio.v1'
    }
}
"""
        )
        createSymLink()
        assert Files.exists(getProjectRoot().toPath().resolve("src").resolve(packageName))
    }

    @Test
    @AccessWeb
    void 'gogs should be built successfully'() {
        // v0.11

        // I don't know why it will fail on Windows
        if (Os.getHostOs() == Os.WINDOWS) {
            IOUtils.append(new File(resource, 'build.gradle'), 'goTest.enabled = false\n')
        }

        init()

        firstBuild()

        File firstBuildResult = getOutputExecutable()
        assert processUtils.getStdout(processUtils.run(firstBuildResult.absolutePath)).contains('Gogs')
        assert new File(resource, 'gogradle.lock').exists()

    }


    void firstBuild() {
        newBuild {
            it.forTasks('goClean', 'goBuild', 'goCheck', 'goLock')
        }
    }

    File getOutputExecutable() {

        println(resource.toPath().resolve(".gogradle/").toFile().listFiles())
        Path gogsBinPath = Paths.get(outputLocation)
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
