package com.github.blindpirate.gogradle.vcs.mercurial

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.vcs.mercurial.client.HgClientMercurialAccessor
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('test-for-gogradle-hg.zip')
class MercurialAccessorTest {

    MercurialAccessor accessor = new HgClientMercurialAccessor()

    File resource

    HgRepository repository

    @Test
    void 'getting remote url should succeed'() {
        assert accessor.getRemoteUrl(resource) == 'https://blindpirate@bitbucket.org/blindpirate/test-for-gogradle'
    }

    @Test
    void 'getting latest commit should succeed'() {
        repository = accessor.getRepository(resource)
        // 2017/2/16 21:45:31 UTC+8
        assert accessor.getLastCommitTimeOfPath(repository, 'commit1') == 1487252731000
    }

    @Test
    void 'finding changeset by tag should succeed'() {
        repository = accessor.getRepository(resource)
        assert accessor.findChangesetByTag(repository, 'commit2_tag').get().id == '1eaebd519f4c3f7d793b9ff42328d4383d672529'
    }

    @Test
    void 'getting head of branch should succeed'() {
        repository = accessor.getRepository(resource)
        assert accessor.headOfBranch(repository, 'default').id == '620889544e2db8b064180431bcd1bb965704f4c2'
    }

    @Test
    @AccessWeb
    void 'pulling should succeed'() {
        repository = accessor.getRepository(resource)
        accessor.pull(repository)
        assert new File(resource, 'commit3').exists()
    }

    @Test
    @AccessWeb
    @WithResource('')
    void 'cloning should succeed'() {
        accessor.cloneWithUrl(resource, 'https://blindpirate@bitbucket.org/blindpirate/test-for-gogradle')
//        accessor.cloneWithUrl(resource, 'ssh://hg@bitbucket.org/blindpirate/test-for-gogradle')
        assert new File(resource, 'commit1').exists()
    }

    @Test
    void 'resetting should succeed'() {
        repository = accessor.getRepository(resource)
        accessor.resetToSpecificNodeId(repository, '5aa103927c66cc82c03161adcacd3a6509859f01')
        assert !new File(resource, 'commit2').exists()
    }
}
