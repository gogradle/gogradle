package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.cache.GlobalCacheMetadata
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Paths

import static java.util.Optional.of
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GlobalCachePackagePathResolverTest {
    @Mock
    GlobalCacheManager cacheManager

    GlobalCachePackagePathResolver resolver

    File resource

    @Before
    void setUp() {
        resolver = new GlobalCachePackagePathResolver(cacheManager)
    }

    @Test
    void 'resolving metadata in global cache should succeed'() {
        // when
        VcsGolangPackage pkg = MockUtils.mockVcsPackage()
        GlobalCacheMetadata metadata = GlobalCacheMetadata.newMetadata(pkg)
        when(cacheManager.getMetadata(Paths.get('github.com/user/package'))).thenReturn(of(metadata))
        GolangPackage info = resolver.produce('github.com/user/package/a/b').get()

        // then
        assert info.vcsType == VcsType.GIT
        assert info.pathString == 'github.com/user/package/a/b'
        assert info.rootPathString == 'github.com/user/package'
        assert info.urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
    }

    @Test
    void 'empty result should be returned if all subpaths are not found'() {
        assert !resolver.produce('inexistent').isPresent()
    }

}
