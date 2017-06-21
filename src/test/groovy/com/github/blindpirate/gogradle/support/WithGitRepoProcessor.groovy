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

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely

class WithGitRepoProcessor extends GogradleRunnerProcessor<WithGitRepo> {
    File repoDir
    GitServer gitServer = GitServer.newServer()

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithGitRepo annotation) {
        setUpGitServer(annotation.repoName(), annotation.fileName())
        ReflectionUtils.setFieldSafely(instance, 'repository', repoDir)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithGitRepo annotation) {
        gitServer.stop()
        deleteQuitely(repoDir)
    }

    File setUpGitServer(String repoName, String fileName) {
        repoDir = tmpRandomDirectory("repository")

        GitServer.createRepository(repoDir, fileName)

        gitServer.addRepo(repoName, repoDir)
        gitServer.start(GitServer.DEFAULT_PORT)
    }
}
