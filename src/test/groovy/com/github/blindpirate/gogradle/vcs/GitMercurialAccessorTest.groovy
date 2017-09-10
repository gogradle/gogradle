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

package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.exceptions.BuildException
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.vcs.git.GitClientLineConsumer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GitMercurialAccessorTest {

    @Mock
    ProcessUtils processUtils
    @Mock
    Process process
    @Mock
    ProcessResult result

    GitMercurialAccessor accessor

    @Before
    void setUp() {
        accessor = new TestGitMercurialAccessor(processUtils)
        result = mock(ProcessResult)
        when(processUtils.run(anyList(), anyMap(), any(File))).thenReturn(process)
        when(processUtils.getResult(process)).thenReturn(result)
    }

    @Test(expected = BuildException)
    void 'exception should be thrown if process ret code is not zero'() {
        // given
        when(result.getCode()).thenReturn(1)
        // when
        accessor.run(new File('.'), [])
    }

    @Test(expected = BuildException)
    void 'exception should be thrown if ret code is not zero when running with progress'() {
        when(process.waitFor()).thenReturn(1)
        accessor.runWithProgress([], GitClientLineConsumer.NO_OP, GitClientLineConsumer.NO_OP)
    }

    @Test(expected = BuildException)
    @WithMockInjector
    void 'exception should be thrown if error occurs in running with progress'() {
        when(process.waitFor()).thenReturn(1)
        accessor.runWithProgress([], GitClientLineConsumer.NO_OP, new GitClientLineConsumer('') {
            @Override
            void accept(String s) {
                throw new IOException()
            }
        })
    }


    class TestGitMercurialAccessor extends GitMercurialAccessor {
        TestGitMercurialAccessor(ProcessUtils processUtils) {
            super(processUtils)
        }

        @Override
        void checkout(File repoRoot, String version) {

        }

        @Override
        String getDefaultBranch(File repoRoot) {
            return null
        }

        @Override
        String getRemoteUrl(File repoRoot) {
            return null
        }

        @Override
        long lastCommitTimeOfPath(File repoRoot, Path relativePath) {
            return 0
        }

        @Override
        Optional<GitMercurialCommit> findCommitByTagOrBranch(File repository, String tag) {
            return null
        }

        @Override
        List<GitMercurialCommit> getAllTags(File repository) {
            return null
        }

        @Override
        Optional<GitMercurialCommit> findCommit(File repository, String commit) {
            return null
        }

        @Override
        GitMercurialCommit headCommitOfBranch(File repository, String branch) {
            return null
        }

        @Override
        void update(File repoRoot) {

        }

        @Override
        void clone(String url, File directory) {

        }
    }
}
