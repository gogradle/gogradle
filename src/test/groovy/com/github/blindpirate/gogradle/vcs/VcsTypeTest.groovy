package com.github.blindpirate.gogradle.vcs

import org.junit.Test

import static com.github.blindpirate.gogradle.vcs.VcsType.*

class VcsTypeTest {

    @Test
    void 'get vcs type by name should success'() {
        assert of('git').get() == VcsType.Git
        assert of('hg').get() == VcsType.Mercurial
        assert of('svn').get() == VcsType.Svn
        assert of('bzr').get() == VcsType.Bazaar

        assert !of('a').isPresent()
    }

    @Test
    void 'test valueOf'() {
        assert VcsType.valueOf('Git') == VcsType.Git
    }

}
