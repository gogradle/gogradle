package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.VcsType
import com.github.blindpirate.gogradle.vcs.git.GitAccessor
import com.google.inject.Injector
import org.eclipse.jgit.lib.Repository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('global-cache-test.zip')
class GlobalCachePackagePathResolverTest {
    @Mock
    GlobalCacheManager cacheManager
    @Mock
    GitAccessor gitAccessor
    @Mock
    Repository repository
    @Mock
    Injector injector

    GlobalCachePackagePathResolver resolver

    File resource

    @Before
    void setUp() {
        resolver = new GlobalCachePackagePathResolver(cacheManager)
        when(cacheManager.getGlobalCachePath(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                String packagePath = invocation.getArgument(0);
                return resource.toPath().resolve(packagePath)
            }
        })


        when(gitAccessor.getRepository(resource.toPath().resolve('github.com/a/b')))
                .thenReturn(repository)
        when(gitAccessor.getRemoteUrls(any(Path))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArgument(0).toString().endsWith('github.com/a/b')) {
                    return ['url']
                } else {
                    throw new IllegalArgumentException()
                }
            }
        })
        MockUtils.mockVcsService(injector, VcsAccessor, Git, gitAccessor)
    }

    @Test
    void 'package should be rejected if it does not exist in global cache'() {
        assert !resolver.produce('a/b/c').isPresent()
    }

    @Test
    void 'resolving root package name should success'() {
        // when
        GolangPackage info = resolver.produce('github.com/a/b').get()

        // then
        assert info.vcsType == VcsType.Git
        assert info.path == 'github.com/a/b'
        assert info.rootPath == 'github.com/a/b'
        assert info.urls.contains('url')
    }

    @Test
    void 'resolving sub package name should success'() {
        // when
        GolangPackage info = resolver.produce('github.com/a/b/c').get()
        // then
        assert info.vcsType == VcsType.Git
        assert info.path == 'github.com/a/b/c'
        assert info.rootPath == 'github.com/a/b'
        assert info.urls.contains('url')
    }

    @Test
    void 'resolving parent of package should return Optional.empty()'() {
        assert !resolver.produce('github.com/a').isPresent()
    }

}
