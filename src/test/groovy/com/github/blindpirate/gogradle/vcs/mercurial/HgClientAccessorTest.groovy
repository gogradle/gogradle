package com.github.blindpirate.gogradle.vcs.mercurial

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.exceptions.BuildException
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.ProcessUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import java.nio.file.Paths
import java.time.Instant

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('test-for-gogradle-hg.zip')
@OnlyWhen(value = '"hg version".execute().text.contains("Mercurial")', ignoreTestWhenException = OnlyWhen.ExceptionStrategy.TRUE)
class HgClientAccessorTest {

    File resource

    HgClientAccessor accessor = new HgClientAccessor(new ProcessUtils())

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if hg client not exists'() {
        // given
        ProcessUtils processUtils = mock(ProcessUtils)
        accessor = new HgClientAccessor(processUtils)
        when(processUtils.runAndGetStdout('hg', 'version')).thenThrow(IOException)

        // then
        accessor.ensureClientExists()
    }

    @Test
    void 'getting remote url should succeed'() {
        assert accessor.getRemoteUrl(resource) == 'https://bitbucket.org/blindpirate/test-for-gogradle'
    }

    @Test
    void 'getting tag list should succeed'() {
        List tags = accessor.getAllTags(resource)
        assert tags.size() == 1
        assert tags[0].tag == 'commit2_tag'
        assert tags[0].id.startsWith('1eae')
        assert Instant.ofEpochMilli(tags[0].commitTime).toString().startsWith('2017-02-16')
    }

    @Test
    void 'getting latest commit should succeed'() {
        // 2017/2/16 21:45:31 UTC+8
        assert accessor.lastCommitTimeOfPath(resource, Paths.get('commit1')) == 1487252731000
    }

    @Test
    void 'finding changeset by tag should succeed'() {
        assert accessor.findCommitByTag(resource, 'commit2_tag').get().id == '1eaebd519f4c3f7d793b9ff42328d4383d672529'
    }

    @Test
    void 'finding changeset by id should succeed'() {
        assert accessor.findCommit(resource, '1eaebd519f4c3f7d793b9ff42328d4383d672529').isPresent()
    }

    @Test
    void 'empty result should be returned if that tag does not exist'() {
        assert !accessor.findCommit(resource, 'unexistent').isPresent()
    }

    @Test
    void 'getting head of branch should succeed'() {
        assert accessor.headCommitOfBranch(resource, 'default').id == '620889544e2db8b064180431bcd1bb965704f4c2'
    }

    @Test
    @AccessWeb
    void 'pulling should succeed'() {
        accessor.pull(resource)
        assert new File(resource, 'commit3').exists()
    }

    @Test
    @AccessWeb
    @WithResource('')
    void 'cloning should succeed'() {
        accessor.clone('https://bitbucket.org/blindpirate/test-for-gogradle', resource)
        assert new File(resource, 'commit1').exists()
    }

    @Test
    void 'resetting should succeed'() {
        accessor.checkout(resource, '5aa103927c66cc82c03161adcacd3a6509859f01')
        assert !new File(resource, 'commit2').exists()
    }

    @Test(expected = BuildException)
    @WithResource('')
    void 'exception should be thrown if no repo exists'() {
        accessor.getRemoteUrl(resource)
    }
}
