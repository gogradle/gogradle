package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VendorMapNotationParserTest {
    @Mock
    MapNotationParser mapNotationParser
    @Mock
    NotationDependency hostDependency

    VendorMapNotationParser parser

    @Before
    void setUp() {
        parser = new VendorMapNotationParser(mapNotationParser)
    }

    @Test
    void 'parsing vendor notation dependency should succeed'() {
        // given
        when(mapNotationParser.parse([name: 'github.com/c/d'])).thenReturn(hostDependency)
        // when
        VendorNotationDependency result = parser.parse([name: 'github.com/a/b', host: [name: 'github.com/c/d'], vendorPath: 'vendor/github.com/a/b'])
        // then
        assert result.hostNotationDependency.is(hostDependency)
        assert result.vendorPath == 'vendor/github.com/a/b'
    }
}
