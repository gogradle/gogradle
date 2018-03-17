package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class VcsPackagePathResolverTest {
    VcsPackagePathResolver resolver = new VcsPackagePathResolver()

    @Test
    void 'path without .vcs should be rejected'() {
        assert resolver.cannotRecognize('a')
        assert resolver.cannotRecognize('')
        assert resolver.cannotRecognize('a.gi')
        assert resolver.cannotRecognize('a.git1')
    }

    @Test
    void 'path with .vcs should be accepted'() {
        assert !resolver.cannotRecognize('.git')
        assert !resolver.cannotRecognize('a.git')
        assert !resolver.cannotRecognize('a.svn')
        assert !resolver.cannotRecognize('a.hg')
        assert !resolver.cannotRecognize('a.bzr')
    }

    @Test
    void 'can not recognize incomplete package'() {
        assert !resolver.isIncomplete('whatever')
        assert !resolver.isIncomplete('pkg.git')
    }

    @Test
    void 'can produce package with .vcs'() {
        verifyVcs(VcsType.GIT, 'a.git/b', 'a.git', ['git://a.git', 'https://a.git', 'http://a.git', 'git+ssh://a.git', 'ssh://a.git'])
        verifyVcs(VcsType.MERCURIAL, 'a.hg/b/c', 'a.hg', ['https://a.hg', 'http://a.hg', 'ssh://a.hg'])
        verifyVcs(VcsType.SVN, 'a.svn/b.git/c.hg/d.bzr/e', 'a.svn', ['https://a.svn', 'http://a.svn', 'svn://a.svn', 'svn+ssh://a.svn'])
        verifyVcs(VcsType.BAZAAR, 'a.bzr/b.bzr/c.bzr/d.bzr', 'a.bzr', ['https://a.bzr', 'http://a.bzr', 'bzr://a.bzr', 'bzr+ssh://a.bzr'])
    }

    void verifyVcs(VcsType vcsType, String packagePath, String rootPath, List<String> urls) {
        VcsGolangPackage pkg = resolver.produce(packagePath).get()
        assert pkg.repository.getVcsType() == vcsType
        assert pkg.repository.original
        assert pkg.repository.urls == urls
        assert pkg.pathString == packagePath
        assert pkg.rootPathString == rootPath
    }
}