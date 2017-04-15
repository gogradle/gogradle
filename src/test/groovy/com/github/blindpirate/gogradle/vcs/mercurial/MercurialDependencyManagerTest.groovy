package com.github.blindpirate.gogradle.vcs.mercurial

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(GogradleRunner)
class MercurialDependencyManagerTest {
    @Mock
    HgClientAccessor hgClientAccessor

    MercurialDependencyManager manager

    @Before
    void setUp() {
        manager = new MercurialDependencyManager(hgClientAccessor, null, null)
    }

    @Test
    void 'getting accessor should succeed'() {
        assert manager.getAccessor().is(hgClientAccessor)
    }

    @Test
    void 'vcs type should be mercurial'() {
        assert manager.getVcsType() == VcsType.MERCURIAL
    }
}
