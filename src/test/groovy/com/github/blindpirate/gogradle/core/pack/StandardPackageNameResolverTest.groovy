package com.github.blindpirate.gogradle.core.pack

import org.junit.Test

class StandardPackageNameResolverTest {
    StandardPackageNameResolver resolver = new StandardPackageNameResolver();

    @Test
    void 'resolving first-level standard package should success'() {
        assert resolver.produce("fmt").get().isStandard()
    }

    @Test
    void 'resolving second-level standard package should success'() {
        assert resolver.produce('archive/zip').get().isStandard()
    }

    @Test
    void 'resolving third-level standard package should success'() {
        assert resolver.produce('net/http/cgi').get().isStandard()
    }

    @Test
    void 'absent value should be returned when resolving non-standard package'() {
        assert !resolver.produce('github.com/a/b').isPresent()
    }
    @Test
    void 'absent value should be returned when resolving relative path'(){
        assert !resolver.produce('./main').isPresent()
        assert !resolver.produce('../main').isPresent()
    }
}
