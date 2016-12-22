package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.util.HttpUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class MetadataPackageNameResolverTest {
    @Mock
    HttpUtils httpUtils

    MetadataPackageNameResolver resolver

    @Before
    void setUp() {
        resolver = new MetadataPackageNameResolver(httpUtils)
    }

    @Test
    void 'get package info from go-import meta tag should success'() {
        // given
        String packageName = 'example.org/pkg/foo'
        String realUrl = 'https://example.org/pkg/foo?go-get=1'
        String metaTag = '<meta name="go-import" content="example.org git https://code.org/r/p/exproj">'
        when(httpUtils.appendQueryParams(anyString(), anyMap())).thenReturn(realUrl)
        when(httpUtils.get(realUrl)).thenReturn(tagInHtml(metaTag))

        // when
        PackageInfo info = resolver.produce(packageName).get()

        // then
        assert info.urls.contains('https://code.org/r/p/exproj')
        assert info.vcsType == VcsType.Git
        assert info.name == packageName
        assert info.rootName == 'example.org'
    }

    @Test
    void 'http should be tried when https failed'() {
        // TODO
    }

    String tagInHtml(String s) {
        return "<html><header>" + s + "</header><body></body></html>";
    }
}
