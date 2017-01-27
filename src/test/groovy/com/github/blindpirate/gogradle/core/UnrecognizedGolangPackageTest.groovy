package com.github.blindpirate.gogradle.core

import org.junit.Test

import static com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage.of

class UnrecognizedGolangPackageTest {
    UnrecognizedGolangPackage unrecognizedGolangPackage = of('golang/x')

    @Test
    void 'path longer than unrecognized package should be empty'() {
        assert !unrecognizedGolangPackage.resolve('golang/x/tools').isPresent()
    }

    @Test
    void 'path shorter than unrecognized package should also be unrecognized'() {
        assert unrecognizedGolangPackage.resolve('golang').get() instanceof UnrecognizedGolangPackage
    }

    @Test(expected = UnsupportedOperationException)
    void 'exception should be thrown when invoking getRootPath()'() {
        unrecognizedGolangPackage.getRootPath()
    }

    @Test(expected = UnsupportedOperationException)
    void 'exception should be thrown when invoking getVcsType()'() {
        unrecognizedGolangPackage.getVcsType()
    }

    @Test(expected = UnsupportedOperationException)
    void 'exception should be thrown when invoking getUrls()'() {
        unrecognizedGolangPackage.getUrls()
    }
}
