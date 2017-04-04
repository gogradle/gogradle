package com.github.blindpirate.gogradle.core

import org.junit.Test

class LocalDirectoryGolangPackageTest {
    @Test
    void 'a local package should resolve its descendants and ancestors successfully'() {
        LocalDirectoryGolangPackage pkg = LocalDirectoryGolangPackage.of('this/is/root', 'this/is/root', '')
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
        LocalDirectoryGolangPackage pkg = LocalDirectoryGolangPackage.of('this/is/root', 'this/is/root/sub', '')
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
}
