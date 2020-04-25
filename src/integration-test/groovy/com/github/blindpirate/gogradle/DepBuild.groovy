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
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path

@RunWith(GogradleRunner)
@Ignore
@OnlyWhen("System.getenv('DEP_DIR')!=null&&'git version'.execute()")
class DepBuild extends IntegrationTestSupport {

    ProcessUtils processUtils = new ProcessUtils()

    @Override
    File getProjectRoot() {
        return new File(System.getenv('DEP_DIR'))
    }

    @Before
    void setUp() {
        processUtils.run(['git', 'reset', "HEAD", '--hard'], null, getProjectRoot())
        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}

golang {
    packagePath="github.com/golang/dep"
    goVersion='1.9'
}

goBuild {
    go 'build -o ./.gogradle/dep github.com/golang/dep/cmd/dep'
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
        File depBinary = new File(projectRoot, '.gogradle/dep')
        Path gogsBinPath = getProjectRoot().toPath().resolve(".gogradle/dep")
        if (Os.getHostOs() == Os.WINDOWS) {
            new File(projectRoot, '.gogradle/dep').toPath().renameTo(depBinary.absolutePath + '.exe')
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

