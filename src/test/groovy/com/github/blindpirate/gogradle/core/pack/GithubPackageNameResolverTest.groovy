package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class GithubPackageNameResolverTest {

    GithubPackageNameResolver resolver = new GithubPackageNameResolver();

    @Test
    void 'resolving name should success'() {
        // when
        PackageInfo result = resolver.produce('github.com/a/b').get()

        // then
        assert result.name == 'github.com/a/b'
        assertVcsTypeUrlsAndRootName(result)
    }

    @Test
    void 'resolving an incomplete name should success'() {
        // when
        PackageInfo result = resolver.produce('github.com/a').get()
        // then
        assert result == PackageInfo.INCOMPLETE
    }


    @Test
    void 'resolving a long name should success'() {
        // when
        PackageInfo info = resolver.produce('github.com/a/b/c').get()

        // then
        assert info.name == 'github.com/a/b/c'
        assertVcsTypeUrlsAndRootName(info)
    }

    @Test
    void 'resolving a long long name should success'() {
        // when
        String wtf = 'github.com/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z'
        PackageInfo info = resolver.produce(wtf).get()

        // then
        assert info.name == wtf
        assertVcsTypeUrlsAndRootName(info)
    }

    void assertVcsTypeUrlsAndRootName(PackageInfo info) {
        assert info.vcsType == VcsType.Git
        assert info.vcsType == VcsType.Git
        assert info.urls.contains('https://github.com/a/b.git')
        assert info.urls.contains('git@github.com:a/b')
        assert info.rootName == 'github.com/a/b'
    }
}
