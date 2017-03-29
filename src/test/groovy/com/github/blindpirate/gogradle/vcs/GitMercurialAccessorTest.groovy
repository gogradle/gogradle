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

import static com.github.blindpirate.gogradle.util.ProcessUtils.*
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
        String getRemoteUrl(File repoRoot) {
            return null
        }

        @Override
        long lastCommitTimeOfPath(File repoRoot, Path relativePath) {
            return 0
        }

        @Override
        Optional<GitMercurialCommit> findCommitByTag(File repository, String tag) {
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
        void pull(File repoRoot) {

        }

        @Override
        void clone(String url, File directory) {

        }
    }
}
