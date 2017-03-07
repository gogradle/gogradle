package com.github.blindpirate.gogradle.vcs.mercurial.client

import org.junit.Test

class HgClientChangesetTest {
    @Test
    void 'smoke test'() {
        HgClientChangeset changeset = HgClientChangeset.of('id', 1L)
        assert changeset.id == 'id'
        assert changeset.commitTime == 1L
    }
}
