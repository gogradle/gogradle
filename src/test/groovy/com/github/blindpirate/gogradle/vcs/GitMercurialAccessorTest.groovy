package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.exceptions.BuildException
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GitMercurialAccessorTest {

    @Mock
    ProcessUtils processUtils
    @Mock
    Process process

    GitMercurialAccessor accessor

    @Before
    void setUp() {
        accessor = new TestGitMercurialAccessor(processUtils)
    }

    @Test(expected = BuildException)
    void 'exception should be thrown if process ret code is not zero'() {
        // given
        ProcessUtils.ProcessResult result = mock(ProcessUtils.ProcessResult)
        when(result.getCode()).thenReturn(1)
        when(processUtils.run(anyList(), anyMap(), any(File))).thenReturn(process)
        when(processUtils.getResult(process)).thenReturn(result)
        // when
        accessor.run(new File('.'), [])
    }

//    @Test(expected = BuildException)
//    void 'exception should be thrown if ret code is not zero when running with progress'() {
//        // given
//        when()
//    }


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
        long lastCommitTimeOfPath(File repoRoot, String relativePath) {
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
