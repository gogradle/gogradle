package com.github.blindpirate.gogradle.vcs.mercurial

import org.junit.Test

class MercurialNotationDependencyTest {
    @Test
    void 'correct resolver should be returned'() {
        assert new MercurialNotationDependency().getResolverClass().is(MercurialDependencyManager)
    }
}
