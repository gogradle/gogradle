package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.cache.CacheManager
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper
import com.github.blindpirate.gogradle.core.dependency.GitDependency
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory
import com.github.blindpirate.gogradle.util.GitUtils
import com.google.common.base.Optional
import com.google.inject.Injector
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path
import java.util.concurrent.Callable

import static com.github.blindpirate.gogradle.vcs.git.GitDependencyResolver.*
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class GitDependencyResolverTest {

    @Mock
    GitDependency dependency
    @Mock
    CacheManager cacheManager
    @Mock
    GitUtils gitUtils
    @Mock
    Repository repository
    @Mock
    Injector injector
    @Mock
    DependencyFactory factory
    @Mock
    RevCommit revCommit
    @InjectMocks
    GitDependencyResolver resolver

    // injected by GogradleRunner
    File resource
    Path path

    @Before
    public void setUp() {
        path = resource.toPath()

        when(injector.getInstance(DependencyFactory)).thenReturn(factory)
        when(cacheManager.getGlobalCachePath(anyString())).thenReturn(path)
        when(gitUtils.getRepository(path)).thenReturn(repository)
        when(gitUtils.hardResetAndUpdate(repository)).thenReturn(repository)
        when(gitUtils.headCommitOfBranch(repository, DEFAULT_BRANCH))
                .thenReturn(Optional.of(revCommit))

        when(dependency.getUrl()).thenReturn("url")
        when(dependency.getName()).thenReturn("name")

        DependencyHelper.INJECTOR_INSTANCE = injector

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

        // when:
        resolver.resolve(dependency)

        // then:
        verify(gitUtils).cloneWithUrl('url', path)
    }


    @Test
    public void 'existed repository should be updated'() {
        path.resolve('placeholder').toFile().createNewFile()

        // given:
        when(gitUtils.getRemoteUrl(repository)).thenReturn(['url'] as Set)

        // when:
        resolver.resolve(dependency)

        // then:
        verify(gitUtils).hardResetAndUpdate(repository)
    }
}
