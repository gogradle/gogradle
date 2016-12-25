package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.InjectionHelper
import com.github.blindpirate.gogradle.core.cache.CacheManager
import com.github.blindpirate.gogradle.core.dependency.GitDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
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

import static com.github.blindpirate.gogradle.vcs.git.GitDependencyResolver.DEFAULT_BRANCH
import static java.util.Optional.of
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class GitDependencyResolverTest {

    @Mock
    GitDependency dependency
    @Mock
    CacheManager cacheManager
    @Mock
    GitAccessor gitAccessor
    @Mock
    Repository repository
    @Mock
    Injector injector
    @Mock
    DependencyFactory factory
    @Mock
    RevCommit revCommit
    @Mock
    GolangDependencySet dependencySet
    @InjectMocks
    GitDependencyResolver resolver

    // injected by GogradleRunner
    File resource
    Path repositoryPath

    @Before
    public void setUp() {
        repositoryPath = resource.toPath()

        when(injector.getInstance(DependencyFactory)).thenReturn(factory)
        when(factory.produce(any(GolangPackageModule))).thenReturn(of(dependencySet))
        when(cacheManager.getGlobalCachePath(anyString())).thenReturn(repositoryPath)
        when(gitAccessor.getRepository(repositoryPath)).thenReturn(repository)
        when(gitAccessor.hardResetAndUpdate(repository)).thenReturn(repository)
        when(gitAccessor.headCommitOfBranch(repository, DEFAULT_BRANCH))
                .thenReturn(of(revCommit))

        when(gitAccessor.getRemoteUrl(repository)).thenReturn("url")
        when(dependency.getName()).thenReturn("name")

        InjectionHelper.INJECTOR_INSTANCE = injector

        when(cacheManager.runWithGlobalCacheLock(any(GitDependency), any(Callable)))
                .thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                invocationOnMock.arguments[1].call()
            }
        })
    }

    @Test
    void 'nonexistent repo should be cloned when user specify a url'() {
        // given:
        when(dependency.getUrl()).thenReturn("url")
        // when:
        resolver.resolve(dependency)
        // then:
        verify(gitAccessor).cloneWithUrl('url', repositoryPath)
    }

    @Test
    void 'multiple urls should be tried to clone the repo'() {
        // given:
        when(dependency.getUrls()).thenReturn(['url1', 'url2'])
        when(gitAccessor.cloneWithUrl('url1', repositoryPath)).thenThrow(new IllegalStateException())
        // when:
        resolver.resolve(dependency)
        // then:
        verify(gitAccessor).cloneWithUrl('url2', repositoryPath)
    }

    @Test
    void 'subsequent url should be ignored if cloning succeed'() {
        // given:
        when(dependency.getUrls()).thenReturn(['url1', 'url2'])
        // when:
        resolver.resolve(dependency)
        // then:
        verify(gitAccessor, times(0)).cloneWithUrl('url2', repositoryPath)

    }

    @Test
    void 'existed repository should be updated'() {
        repositoryPath.resolve('placeholder').toFile().createNewFile()

        // given:
        when(gitAccessor.getRemoteUrls(repository)).thenReturn(['url'] as Set)
        when(dependency.getUrl()).thenReturn('url')
        // when:
        resolver.resolve(dependency)
        // then:
        verify(gitAccessor).hardResetAndUpdate(repository)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown when every url has been tried'() {
        // given
        when(dependency.getUrls()).thenReturn(['url1', 'url2'])
        ['url1', 'url2'].each {
            when(gitAccessor.cloneWithUrl(it, repositoryPath)).thenThrow(new IllegalStateException())
        }

        // when
        resolver.resolve(dependency)
    }

    @Test
    void 'resetting to a commit should success'() {
        // given
        revCommit = RevCommit.parse([48] * 64 as byte[])
        when(dependency.getCommit()).thenReturn(revCommit.name)
        when(gitAccessor.findCommit(repository, revCommit.name)).thenReturn(of(revCommit))
        // when
        resolver.resolve(dependency)
        // then
        verify(gitAccessor).resetToCommit(repository, revCommit.name)
    }

    @Test(expected = DependencyResolutionException)
    void 'trying to resolve an inexistent commit should result in an exception'() {
        // given
        revCommit = RevCommit.parse([48] * 64 as byte[])
        when(dependency.getCommit()).thenReturn(revCommit.name)
        // when
        resolver.resolve(dependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception in locked block should not be swallowed'() {
        // given
        when(cacheManager.runWithGlobalCacheLock(any(GitDependency), any(Callable)))
                .thenThrow(new IOException())
        // when
        resolver.resolve(dependency)
    }

}
