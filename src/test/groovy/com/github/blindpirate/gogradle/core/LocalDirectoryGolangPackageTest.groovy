package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

import static com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage.*

class LocalDirectoryGolangPackageTest {
    @Test
    void 'a local package should resolve its descendants and ancestors successfully'() {
        LocalDirectoryGolangPackage pkg = of('this/is/root', 'this/is/root', '')
        GolangPackage desc = pkg.resolve('this/is/root/sub').get()
        GolangPackage ance = pkg.resolve('this/is').get()
        assert desc instanceof LocalDirectoryGolangPackage
        assert desc.pathString == 'this/is/root/sub'
        assert desc.rootPathString == 'this/is/root'
        assert desc.dir == ''

        assert ance instanceof IncompleteGolangPackage
        assert ance.pathString == 'this/is'
    }

    @Test
    void 'a local sub package should resolve its descendants and ancestors successfully'() {
        LocalDirectoryGolangPackage pkg = of('this/is/root', 'this/is/root/sub', '')
        GolangPackage desc = pkg.resolve('this/is/root/sub/sub').get()
        GolangPackage ance = pkg.resolve('this/is').get()
        GolangPackage root = pkg.resolve('this/is/root').get()

        assert desc instanceof LocalDirectoryGolangPackage
        assert desc.pathString == 'this/is/root/sub/sub'
        assert desc.rootPathString == 'this/is/root'
        assert desc.dir == ''

        assert root instanceof LocalDirectoryGolangPackage
        assert root.pathString == 'this/is/root'
        assert root.rootPathString == 'this/is/root'
        assert root.dir == ''

        assert ance instanceof IncompleteGolangPackage
        assert ance.pathString == 'this/is'
    }

    @Test
    void 'equality check should succeed'() {
        LocalDirectoryGolangPackage pkg = of('this/is/root', 'this/is/root/sub', '')
        assert pkg != null
        assert pkg == pkg
        assert pkg != VcsGolangPackage.builder()
                .withRootPath('this/is/root')
                .withPath("this/is/root/sub")
                .withOriginalVcsInfo(VcsType.GIT, ['url'])
                .build()
        assert pkg == of('this/is/root', 'this/is/root/sub', '')
        assert pkg != of('this/is/root', 'this/is/root', '')
        assert pkg != of('this/is', 'this/is/root/sub', '')
        assert pkg != of('this/is/root', 'this/is/root/sub', 'anotherDir')
    }
}
