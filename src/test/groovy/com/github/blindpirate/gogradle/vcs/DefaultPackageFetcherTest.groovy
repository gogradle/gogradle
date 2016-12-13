package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class DefaultPackageFetcherTest {
    @Mock
    GoImportMetadataFetcher goImportMetadataFetcher
    @Mock
    PackageFetcher githubPackageFetcher
    @Mock
    Path path

    DefaultPackageFetcher defaultPackageFetcher

    @Before
    void setUp() {
        defaultPackageFetcher = new DefaultPackageFetcher(
                ['github.com': githubPackageFetcher],
                goImportMetadataFetcher)
    }

    @Test
    void 'package with known host should be delegated to corresponding fetcher'() {
        // when
        defaultPackageFetcher.fetch('github.com/a/b', path)
        // then
        verify(githubPackageFetcher).fetch('github.com/a/b', path);
    }

    @Test
    void 'package with unknown host should be fetched by GoImportMetadataFetcher'() {
        // when
        defaultPackageFetcher.fetch('wtf.com/a/b/c', path)
        // then
        verify(goImportMetadataFetcher).fetch('wtf.com/a/b/c', path)

    }
}
