package com.github.blindpirate.gogradle.vcs.mercurial

import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class MercurialResolvedDependencyTest {
    @Test
    void 'correct installer class should be returned'() {
        assert new MercurialResolvedDependency(null, null, 0L).installerClass == MercurialDependencyManager.class
    }

    @Test
    void 'correct vcs type should be returned'() {
        assert new MercurialResolvedDependency(null, null, 0L).vcsType == VcsType.MERCURIAL
    }
}
