package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class VcsGolangPackageTest {
    VcsGolangPackage vcsGolangPackage = MockUtils.mockVcsPackage()

    @Test
    void 'toString should print properties successfully'() {
        String s = vcsGolangPackage.toString()
        assert s.contains("path='github.com/user/package/a'")
        assert s.contains('vcsType=GIT')
        assert s.contains('https://github.com/user/package.git')
        assert s.contains("rootPath='github.com/user/package'")
    }

    @Test
    void 'path shorter than root should be incomplete'() {
        assert vcsGolangPackage.resolve('github.com').get() instanceof IncompleteGolangPackage
        assert vcsGolangPackage.resolve('github.com/user').get() instanceof IncompleteGolangPackage
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if resolving an package without common prefix'() {
        assert vcsGolangPackage.resolve('github.com/anotheruser')
    }

    @Test
    void 'path longer than root should have have the same root'() {
        // when
        GolangPackage packageWithLongerPath = vcsGolangPackage.resolve('github.com/user/package/a/b').get()
        // then
        assert packageWithLongerPath.pathString == 'github.com/user/package/a/b'
        assertRootPathAndSoOn(packageWithLongerPath)
    }

    @Test
    void 'path equal to root should have have the same root'() {
        // when
        GolangPackage rootPackage = vcsGolangPackage.resolve('github.com/user/package').get()
        // then
        assert rootPackage.pathString == 'github.com/user/package'
        assertRootPathAndSoOn(rootPackage)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if both originalInfo and substitutedInfo are null'() {
        VcsGolangPackage.builder().withPath('root')
                .withRootPath('root')
                .build()
    }

    @Test
    void 'equality check should succeed'() {
        assert !vcsGolangPackage.equals(null)
        assert vcsGolangPackage.equals(vcsGolangPackage)
        assert vcsGolangPackage != LocalDirectoryGolangPackage.of('github.com/user/package', 'github.com/user/pavkage/a', '')
        assert vcsGolangPackage == MockUtils.mockVcsPackage()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withOriginalVcsInfo(VcsType.MERCURIAL, ['git@github.com:user/package.git', 'https://github.com/user/package.git'])
                .build()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withOriginalVcsInfo(VcsType.GIT, [])
                .build()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/b')
                .withRootPath('github.com/user/package')
                .withOriginalVcsInfo(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git'])
                .build()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user')
                .withOriginalVcsInfo(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git'])
                .build()
        assert vcsGolangPackage == VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withSubstitutedVcsInfo(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git'])
                .build()
    }

    @Test
    void 'hashCode should succeed'() {
        assert vcsGolangPackage.hashCode() != LocalDirectoryGolangPackage.of('github.com/user/package', 'github.com/user/pavkage/a', '').hashCode()
        assert vcsGolangPackage.hashCode() == MockUtils.mockVcsPackage().hashCode()
    }

    void assertRootPathAndSoOn(GolangPackage golangPackage) {
        assert golangPackage instanceof VcsGolangPackage
        assert golangPackage.rootPathString == 'github.com/user/package'
        assert golangPackage.vcsType == VcsType.GIT
        assert golangPackage.urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
    }
}
