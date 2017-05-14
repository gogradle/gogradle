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

package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.Assert
import com.github.blindpirate.gogradle.util.CompressUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely

class WithGitReposProcessor extends GogradleRunnerProcessor<WithGitRepos> {
    File resourceDir
    GitServer gitServer = GitServer.newServer()

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithGitRepos annotation) {
        if (annotation.value() != '') {
            setUpGitServerWithZip(annotation.value())
        } else {
            setUpGitServerFromScratch(annotation.repoNames(), annotation.fileNames())
        }
        ReflectionUtils.setFieldSafely(instance, 'repositories', resourceDir)
    }

    def setUpGitServerFromScratch(String[] repoNames, String[] fileNames) {
        resourceDir = tmpRandomDirectory("repositories")
        (0..<repoNames.size()).each {
            File dir = new File(resourceDir, repoNames[it])
            dir.mkdir()
            GitServer.createRepository(dir, fileNames[it])
            gitServer.addRepo(repoNames[it], dir)
        }
        gitServer.start(GitServer.DEFAULT_PORT)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithGitRepos annotation) {
        gitServer.stop()
        deleteQuitely(resourceDir)
    }

    File setUpGitServerWithZip(String resourceName) {
        File destDir = tmpRandomDirectory("repositories")
        Assert.isTrue(resourceName.endsWith('.zip'))
        decompressResourceToDir(resourceName, destDir)
        resourceDir = destDir

        resourceDir.listFiles().each {
            if (it.isDirectory()) {
                gitServer.addRepo(it.name, it)
            }
        }

        gitServer.start(GitServer.DEFAULT_PORT)
    }

    def decompressResourceToDir(String resourceName, File destDir) {
        File resource = new File(this.class.classLoader.getResource(resourceName).toURI())
        CompressUtils.decompressZipOrTarGz(resource, destDir)
    }
}
