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
@OnlyWhen("System.getenv('DEP_DIR')")
class DepBuild extends IntegrationTestSupport {

    File resource
    def outputLocation
    def packageName = 'github.com/golang/dep'
    ProcessUtils processUtils = new ProcessUtils()

    def createSymLink() {
        Path link = Paths.get(resource.getAbsolutePath(), "src", packageName)
        Path targetDir = Paths.get(System.getenv('DEP_DIR'))
        if (!link.getParent().toFile().exists()) {
            IOUtils.forceMkdir(link.getParent().toFile())
            Files.createSymbolicLink(link, targetDir)
        }
        assert link.toFile().exists()
    }

    @Override
    File getProjectRoot() {
        return resource
    }



    @Before
    void setUp() {
        outputLocation="${resource.toPath().toAbsolutePath().toString()}/build/bin/dep"
        createSymLink()
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}

golang {
    packagePath="${packageName}"
    goVersion='1.9'
    applicationName ='dep'
    
}

goBuild {
    outputLocation="${outputLocation}"
}

goTest.enabled = false
""")
    }

    @Test
    void 'building dep should succeed'() {
        init()
        try {
            firstBuild()
            verifyDepBin()
            secondBuild()
            verifyDepBin()
        } finally {
            println(stdout)
            println(stderr)
        }
    }

    def verifyDepBin() {
        File depBinary = new File(outputLocation)
        Path gogsBinPath = Paths.get(outputLocation)
        if (Os.getHostOs() == Os.WINDOWS) {
            new File(outputLocation).toPath().renameTo(depBinary.absolutePath + '.exe')
        }
        IOUtils.chmodAddX(gogsBinPath)
        // I don't know why dep prints command to stderr...
        assert processUtils.runAndGetStderr(depBinary.absolutePath).contains('Dep is a tool for managing dependencies for Go projects')
    }

    void firstBuild() {
        newBuild('goClean', 'goBuild', 'goLock')
    }

    void secondBuild() {
        newBuild('goBuild')
    }
}

