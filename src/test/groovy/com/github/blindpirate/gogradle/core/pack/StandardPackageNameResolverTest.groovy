package com.github.blindpirate.gogradle.core.pack

import org.junit.Test

class StandardPackageNameResolverTest {
    StandardPackageNameResolver resolver = new StandardPackageNameResolver();

    @Test
    void 'resolving first-level standard package should success'() {
        PackageInfo info = resolver.produce("fmt").get()
        assert info.isStandard()
        assert info.name == 'fmt'
        assert info.rootName == 'fmt'
    }

    @Test
    void 'resolving second-level standard package should success'() {
        PackageInfo info = resolver.produce('archive/zip').get()
        assert info.isStandard()
        assert info.name == 'archive/zip'
        assert info.rootName == 'archive/zip'
    }

    @Test
    void 'resolving third-level standard package should success'() {
        PackageInfo info = resolver.produce('net/http/cgi').get()
        assert info.isStandard()
        assert info.name == 'net/http/cgi'
        assert info.rootName == 'net/http/cgi'
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
