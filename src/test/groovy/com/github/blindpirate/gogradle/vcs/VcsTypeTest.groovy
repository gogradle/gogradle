package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(GogradleRunner)
class VcsTypeTest {
    @Mock
    PackageFetcher gitPackageFetcher

    @Test
    public void 'get vcs type by name should success'() {
        assert VcsType.of('git').get() == VcsType.Git
        assert VcsType.of('hg').get() == VcsType.Mercurial
        assert VcsType.of('svn').get() == VcsType.Svn
        assert VcsType.of('bzr').get() == VcsType.Bazaar

        assert VcsType.ofDotSuffix('.git').get() == VcsType.Git
        assert VcsType.ofDotSuffix('.hg').get() == VcsType.Mercurial
        assert VcsType.ofDotSuffix('.svn').get() == VcsType.Svn
        assert VcsType.ofDotSuffix('.bzr').get() == VcsType.Bazaar

        assert !VcsType.of('a').isPresent()
        assert !VcsType.ofDotSuffix('.a').isPresent()
    }

    @Test(expected = IllegalStateException)
    public void "vcs's fetcher can only be set once"() {
        VcsType.Git.setFetcher(gitPackageFetcher)
        VcsType.Git.setFetcher(gitPackageFetcher)
    }
}
