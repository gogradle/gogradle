package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import org.junit.Test

class UnrecognizedPackagePathResolverTest {
    UnrecognizedPackagePathResolver resolver = new UnrecognizedPackagePathResolver()

    @Test
    void 'unrecognized package should be resolved'() {
        // when
        GolangPackage pkg = resolver.produce('any').get()
        // then
        assert pkg instanceof UnrecognizedGolangPackage
        assert pkg.path == 'any'
    }


}
