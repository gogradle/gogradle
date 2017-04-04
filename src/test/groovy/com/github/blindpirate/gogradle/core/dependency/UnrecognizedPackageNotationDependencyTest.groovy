package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import org.junit.Test

class UnrecognizedPackageNotationDependencyTest {

    UnrecognizedGolangPackage pkg = UnrecognizedGolangPackage.of('unrecognized')

    UnrecognizedPackageNotationDependency dependency = UnrecognizedPackageNotationDependency.of(pkg)

    @Test(expected = UnsupportedOperationException)
    void 'isFirstLevel is not supported'() {
        dependency.isFirstLevel()
    }

    @Test(expected = UnsupportedOperationException)
    void 'getTransitiveDepExclusions is not supported'() {
        dependency.getTransitiveDepExclusions()
    }

    @Test(expected = UnsupportedOperationException)
    void 'resolve is not supported'() {
        dependency.resolve(null)
    }

    @Test
    void 'getting package should succeed'() {
        assert dependency.package.is(pkg)
    }
}
