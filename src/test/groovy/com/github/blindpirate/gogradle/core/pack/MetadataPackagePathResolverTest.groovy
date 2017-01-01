package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.util.HttpUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class MetadataPackagePathResolverTest {
    @Mock
    HttpUtils httpUtils

    MetadataPackagePathResolver resolver

    @Before
    void setUp() {
        resolver = new MetadataPackagePathResolver(httpUtils)
        when(httpUtils.appendQueryParams(anyString(), anyMap())).thenCallRealMethod()
    }

    @Test
    void 'get package info from go-import meta tag should success'() {
        // given
        String packagePath = 'example.org/pkg/foo'
        String realUrl = 'https://example.org/pkg/foo?go-get=1'
        String metaTag = '<meta name="go-import" content="example.org git https://code.org/r/p/exproj">'
        when(httpUtils.get(realUrl)).thenReturn(tagInHtml(metaTag))

        // when
        GolangPackage info = resolver.produce(packagePath).get()

        // then
        assert info.urls.contains('https://code.org/r/p/exproj')
        assert info.vcsType == VcsType.Git
        assert info.path == packagePath
        assert info.rootPath == 'example.org'
    }

    @Test
    void 'http should be tried when https failed'() {
        // TODO
        // given
        String packagePath = 'example.org/pkg/foo'
        String realHttpsUrl = 'https://example.org/pkg/foo?go-get=1'
        String realHttpUrl = 'http://example.org/pkg/foo?go-get=1'
        String metaTag = '<meta name="go-import" content="example.org git https://code.org/r/p/exproj">'
        when(httpUtils.get(realHttpsUrl)).thenThrow(new IOException())
        when(httpUtils.get(realHttpUrl)).thenReturn(metaTag)

        // when
        resolver.produce(packagePath).get()

        // then
        verify(httpUtils).get(realHttpUrl)
    }

    String tagInHtml(String s) {
        return "<html><header>" + s + "</header><body></body></html>";
    }
}
