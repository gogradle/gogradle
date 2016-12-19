package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.pack.PackageInfo
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver
import com.github.blindpirate.gogradle.util.MockUtils
import com.google.inject.Injector
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultPackageFetcherTest {
    @Mock
    PackageNameResolver packageNameResolver
    @Mock
    PackageFetcher vcsFetcher
    @Mock
    Path path
    @Mock
    PackageInfo packageInfo
    @Mock
    Injector injector

    DefaultPackageFetcher defaultPackageFetcher

    @Before
    void setUp() {
        defaultPackageFetcher = new DefaultPackageFetcher(packageNameResolver)
    }

    @Test
    void 'package should be delegated to corresponding fetcher'() {
        String packageName = 'package';
        // given
        when(packageNameResolver.produce(packageName)).thenReturn(packageInfo)
        when(packageInfo.getVcsType()).thenReturn(VcsType.Git)
        MockUtils.mockVcsService(injector, PackageFetcher, Git, vcsFetcher)
        // when
        defaultPackageFetcher.fetch(packageName, path)
        // then
        verify(vcsFetcher).fetch(packageName, path);
    }
}
