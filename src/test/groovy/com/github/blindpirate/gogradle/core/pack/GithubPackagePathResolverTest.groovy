package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class GithubPackagePathResolverTest {

    GithubPackagePathResolver resolver = new GithubPackagePathResolver()

    @Test
    void 'resolving name should succeed'() {
        // when
        GolangPackage result = resolver.produce('github.com/a/b').get()

        // then
        assert result.path == 'github.com/a/b'
        assertVcsTypeUrlsAndRootPath(result)
    }

    @Test
    void 'resolving an incomplete name should succeed'() {
        // when
        GolangPackage result = resolver.produce('github.com/a').get()
        // then
        assert result instanceof IncompleteGolangPackage
    }


    @Test
    void 'resolving a long name should succeed'() {
        // when
        GolangPackage info = resolver.produce('github.com/a/b/c').get()

        // then
        assert info.path == 'github.com/a/b/c'
        assertVcsTypeUrlsAndRootPath(info)
    }

    @Test
    void 'resolving a long long name should succeed'() {
        // when
        String wtf = 'github.com/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z'
        GolangPackage info = resolver.produce(wtf).get()

        // then
        assert info.path == wtf
        assertVcsTypeUrlsAndRootPath(info)
    }

    @Test
    void 'empty result should be returned if it does not host on github'() {
        assert !resolver.produce('golang.org/x/tool').isPresent()
    }

    void assertVcsTypeUrlsAndRootPath(GolangPackage info) {
        assert info.vcsType == VcsType.GIT
        assert info.vcsType == VcsType.GIT
        assert info.url == 'git@github.com:a/b.git'
        assert info.rootPath == 'github.com/a/b'
    }
}
