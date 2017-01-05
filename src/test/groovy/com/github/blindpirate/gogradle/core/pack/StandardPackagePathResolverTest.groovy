package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.core.GolangPackage
import org.junit.Test

class StandardPackagePathResolverTest {
    StandardPackagePathResolver resolver = new StandardPackagePathResolver()

    @Test
    void 'resolving first-level standard package should success'() {
        GolangPackage info = resolver.produce("fmt").get()
        assert info.isStandard()
        assert info.path == 'fmt'
        assert info.rootPath == 'fmt'
    }

    @Test
    void 'resolving second-level standard package should success'() {
        GolangPackage info = resolver.produce('archive/zip').get()
        assert info.isStandard()
        assert info.path == 'archive/zip'
        assert info.rootPath == 'archive'
    }

    @Test
    void 'resolving third-level standard package should success'() {
        GolangPackage info = resolver.produce('net/http/cgi').get()
        assert info.isStandard()
        assert info.path == 'net/http/cgi'
        assert info.rootPath == 'net'
    }

    @Test
    void 'resolving pseudo package "C" should succss'() {
        GolangPackage info = resolver.produce('C').get()
        assert info.isStandard()
        assert info.path == 'C'
        assert info.rootPath == 'C'
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
