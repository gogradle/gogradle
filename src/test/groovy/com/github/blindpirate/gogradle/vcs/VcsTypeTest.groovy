package com.github.blindpirate.gogradle.vcs

import org.junit.Test

import static com.github.blindpirate.gogradle.vcs.VcsType.*

class VcsTypeTest {

    @Test
    void 'get vcs type by name should succeed'() {
        assert of('git').get() == VcsType.GIT
        assert of('hg').get() == VcsType.MERCURIAL
        assert of('svn').get() == VcsType.SVN
        assert of('bzr').get() == VcsType.BAZAAR

        assert !of('a').isPresent()
    }

    @Test
    void 'test valueOf'() {
        assert VcsType.valueOf('GIT') == VcsType.GIT
    }

}
