package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.util.HttpUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import static com.github.blindpirate.gogradle.vcs.PackageFetcher.HTTPS
import static org.mockito.Matchers.anyMap
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GoImportMetadataFetcherTest {
    @InjectMocks
    GoImportMetadataFetcher goImportFetcher
    @Mock
    HttpUtils httpUtils
    @Mock
    PackageFetcher git
    @Mock
    PackageFetcher svn
    @Mock
    PackageFetcher hg
    @Mock
    PackageFetcher bzr


    public GoImportMetadataFetcherTest() {
        VcsType.Git.setFetcher(git)
        VcsType.Svn.setFetcher(svn)
        VcsType.Mercurial.setFetcher(hg)
        VcsType.Bazaar.setFetcher(bzr)
    }

    @Test
    public void 'extract info from go-import meta tag should success'() {
        ensurePackageResolvedProperly('example.org/pkg/foo',
                git,
                '<meta name="go-import" content="example.org git https://code.org/r/p/exproj">',
                'https://code.org/r/p/exproj'
        );

    }

    private ensurePackageResolvedProperly(String packageName, PackageFetcher fetcher, String metaTag, String realUrl) {

        when(httpUtils.get(eq(HTTPS + packageName), anyMap())).thenReturn(tagInHtml(metaTag))

        goImportFetcher.fetch(packageName, null)

        verify(fetcher).fetch(realUrl, null)
    }

    String tagInHtml(String s) {
        return "<html><header>" + s + "</header><body></body></html>";
    }
}
