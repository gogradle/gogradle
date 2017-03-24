package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.StandardGolangPackage
import org.junit.Test

class StandardPackagePathResolverTest {
    StandardPackagePathResolver resolver = new StandardPackagePathResolver()

    @Test
    void 'resolving first-level standard package should succeed'() {
        GolangPackage info = resolver.produce("fmt").get()
        assert info instanceof StandardGolangPackage
        assert info.pathString == 'fmt'
        assert info.rootPathString == 'fmt'
    }

    @Test
    void 'resolving second-level standard package should succeed'() {
        GolangPackage info = resolver.produce('archive/zip').get()
        assert info instanceof StandardGolangPackage
        assert info.pathString == 'archive/zip'
        assert info.rootPathString == 'archive/zip'
    }

    @Test
    void 'resolving third-level standard package should succeed'() {
        GolangPackage info = resolver.produce('net/http/cgi').get()
        assert info instanceof StandardGolangPackage
        assert info.pathString == 'net/http/cgi'
        assert info.rootPathString == 'net/http/cgi'
    }

    @Test
    void 'resolving pseudo package "C" should succeed'() {
        GolangPackage info = resolver.produce('C').get()
        assert info instanceof StandardGolangPackage
        assert info.pathString == 'C'
        assert info.rootPathString == 'C'
    }

    @Test
    void 'absent value should be returned when resolving non-standard package'() {
        assert !resolver.produce('github.com/a/b').isPresent()
    }

    @Test
    void 'absent value should be returned when resolving relative path'() {
        assert !resolver.produce('./main').isPresent()
        assert !resolver.produce('../main').isPresent()
    }
}
