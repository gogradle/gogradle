package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.vcs.VcsType.*

@RunWith(GogradleRunner)
class VcsTypeTest {
    @Mock
    PackageFetcher gitPackageFetcher

    @Test
    public void 'get vcs type by name should success'() {
        assert of('git').get() == Git
        assert of('hg').get() == Mercurial
        assert of('svn').get() == Svn
        assert of('bzr').get() == Bazaar

        assert ofDotSuffix('.git').get() == Git
        assert ofDotSuffix('.hg').get() == Mercurial
        assert ofDotSuffix('.svn').get() == Svn
        assert ofDotSuffix('.bzr').get() == Bazaar

        assert !of('a').isPresent()
        assert !ofDotSuffix('.a').isPresent()
    }

}
