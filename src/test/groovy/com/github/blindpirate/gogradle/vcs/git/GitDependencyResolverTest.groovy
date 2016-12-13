package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.cache.CacheManager
import com.github.blindpirate.gogradle.core.dependency.GitDependency
import com.github.blindpirate.gogradle.util.GitUtils
import org.eclipse.jgit.lib.Repository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path
import java.util.concurrent.Callable

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class GitDependencyResolverTest {

    @Mock
    GitDependency dependency

    @InjectMocks
    GitDependencyResolver resolver

    @Mock
    CacheManager cacheManager

    @Mock
    GitUtils gitUtils

    @Mock
    Repository repository

    // injected by GogradleRunner
    File resource

    @Before
    public void setUp() {
        when(cacheManager.runWithGlobalCacheLock(any(GitDependency), any(Callable)))
                .thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                invocationOnMock.arguments[1].call()
            }
        })
    }

    @Test
    public void 'nonexistent repo should be cloned'() {
        Path path = resource.toPath()
        // given:
        when(cacheManager.getGlobalCachePath(anyString())).thenReturn(path)

        when(dependency.getUrl()).thenReturn("url")

        // when:
        resolver.resolve(dependency)

        // then:
        verify(gitUtils).cloneWithUrl('url', path)
    }


    @Test
    public void 'existed repository should be updated'() {
        Path path = resource.toPath()
        path.resolve('placeholder').toFile().createNewFile()

        // given:
        when(cacheManager.getGlobalCachePath(anyString())).thenReturn(path)
        when(gitUtils.getRepository(path)).thenReturn(repository)
        when(gitUtils.getRemoteUrl(repository)).thenReturn(['url'] as Set)
        when(dependency.getUrl()).thenReturn('url')

        // when:
        resolver.resolve(dependency)

        // then:
        verify(gitUtils).hardResetAndUpdate(repository)


    }
}
