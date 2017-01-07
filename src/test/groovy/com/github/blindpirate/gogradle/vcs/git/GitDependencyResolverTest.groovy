package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.InjectionHelper
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy
import com.github.blindpirate.gogradle.core.exceptions.DependencyInstallationException
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.util.IOUtils
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

import java.util.concurrent.Callable

import static com.github.blindpirate.gogradle.util.DependencyUtils.mockWithName
import static com.github.blindpirate.gogradle.vcs.git.GitDependencyResolver.DEFAULT_BRANCH
import static java.util.Optional.empty
import static java.util.Optional.of
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class GitDependencyResolverTest {

    GitNotationDependency notationDependency = mockWithName(GitNotationDependency, 'name')
    GitResolvedDependency resolvedDependency = mockWithName(GitResolvedDependency, 'name')
    @Mock
    GlobalCacheManager cacheManager
    @Mock
    GitAccessor gitAccessor
    @Mock
    Repository repository
    @Mock
    Injector injector
    @Mock
    GolangDependencySet dependencySet
    @Mock
    DependencyProduceStrategy strategy
    @InjectMocks
    GitDependencyResolver resolver
    // this is a fake commit. We cannot mock RevCommit directly because RevCommit.getName() is final
    RevCommit revCommit = RevCommit.parse([48] * 64 as byte[])

    File resource

    @Before
    void setUp() {
        when(cacheManager.getGlobalCachePath(anyString())).thenReturn(resource.toPath())
        when(gitAccessor.getRepository(resource)).thenReturn(repository)
        when(gitAccessor.hardResetAndUpdate(repository)).thenReturn(repository)
        when(gitAccessor.headCommitOfBranch(repository, DEFAULT_BRANCH))
                .thenReturn(of(revCommit))

        when(gitAccessor.getRemoteUrl(repository)).thenReturn("url")
        when(notationDependency.getStrategy()).thenReturn(strategy)
        when(strategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor)))
                .thenReturn(GolangDependencySet.empty())

        InjectionHelper.INJECTOR_INSTANCE = injector

        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable)))
                .thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                invocationOnMock.arguments[1].call()
            }
        })
    }

    // TODO we need an integration test to test
    // both GitDependencyResolver and GitAccessor

    @Test
    void 'nonexistent repo should be cloned when user specify a url'() {
        // given:
        when(notationDependency.getUrl()).thenReturn("url")
        // when:
        resolver.resolve(notationDependency)
        // then:
        verify(gitAccessor).cloneWithUrl('url', resource)
    }

    @Test
    void 'multiple urls should be tried to clone the repo'() {
        // given:
        when(notationDependency.getUrls()).thenReturn(['url1', 'url2'])
        when(gitAccessor.cloneWithUrl('url1', resource)).thenThrow(new IllegalStateException())
        // when:
        resolver.resolve(notationDependency)
        // then:
        verify(gitAccessor).cloneWithUrl('url2', resource)
    }

    @Test
    void 'subsequent url should be ignored if cloning succeed'() {
        // given:
        when(notationDependency.getUrls()).thenReturn(['url1', 'url2'])
        // when:
        resolver.resolve(notationDependency)
        // then:
        verify(gitAccessor, times(0)).cloneWithUrl('url2', resource)
    }

    @Test
    void 'existed repository should be updated'() {
        IOUtils.write(resource, 'placeholder', '')

        // given:
        when(gitAccessor.getRemoteUrls(repository)).thenReturn(['url'] as Set)
        when(notationDependency.getUrl()).thenReturn('url')
        // when:
        resolver.resolve(notationDependency)
        // then:
        verify(gitAccessor).hardResetAndUpdate(repository)
    }

    @Test
    void 'dependency with tag should be resolved successfully'() {
        // given
        when(notationDependency.getTag()).thenReturn('tag')
        when(gitAccessor.findCommitByTag(repository, 'tag')).thenReturn(of(revCommit))
        // when
        resolver.resolve(notationDependency)
        // then
        verify(gitAccessor).resetToCommit(repository, revCommit.getName())
    }

    @Test
    void 'tag should be interpreted as sem version if commit not found'() {
        // given
        when(notationDependency.getTag()).thenReturn('semversion')
        when(gitAccessor.findCommitByTag(repository, 'semversion')).thenReturn(empty())
        when(gitAccessor.findCommitBySemVersion(repository, 'semversion')).thenReturn(of(revCommit))
        // when
        resolver.resolve(notationDependency)
        // then
        verify(gitAccessor).resetToCommit(repository, revCommit.getName())
    }

    @Test
    void 'commit will be searched if tag cannot be recognized'() {
        // given
        when(notationDependency.getTag()).thenReturn('tag')
        // when
        resolver.resolve(notationDependency)
        // then
        verify(gitAccessor).headCommitOfBranch(repository, 'master')
    }

    @Test
    void 'NEWEST_COMMIT should be recognized properly'() {
        // given
        when(notationDependency.getCommit()).thenReturn(GitNotationDependency.NEWEST_COMMIT)
        // when
        resolver.resolve(notationDependency)
        // then
        verify(gitAccessor).headCommitOfBranch(repository, 'master')
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown when every url has been tried'() {
        // given
        when(notationDependency.getUrls()).thenReturn(['url1', 'url2'])
        ['url1', 'url2'].each {
            when(gitAccessor.cloneWithUrl(it, resource)).thenThrow(new IllegalStateException())
        }

        // when
        resolver.resolve(notationDependency)
    }

    @Test
    void 'resetting to a commit should succeed'() {
        // given
        when(notationDependency.getCommit()).thenReturn(revCommit.name)
        when(gitAccessor.findCommit(repository, revCommit.name)).thenReturn(of(revCommit))
        // when
        resolver.resolve(notationDependency)
        // then
        verify(gitAccessor).resetToCommit(repository, revCommit.name)
    }

    @Test(expected = DependencyResolutionException)
    void 'trying to resolve an inexistent commit should result in an exception'() {
        // given
        revCommit = RevCommit.parse([48] * 64 as byte[])
        when(notationDependency.getCommit()).thenReturn(revCommit.name)
        // when
        resolver.resolve(notationDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception in locked block should not be swallowed'() {
        // given
        when(cacheManager.runWithGlobalCacheLock(any(GitNotationDependency), any(Callable)))
                .thenThrow(new IOException())
        // when
        resolver.resolve(notationDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'mismatched repository should cause an exception'() {
        // given
        when(notationDependency.getUrls()).thenReturn(['anotherUrl'])
        IOUtils.write(resource, 'some file', 'file content')

        // when
        resolver.resolve(notationDependency)
    }

    @Test
    void 'resetting a resolved dependency should succeed'() {
        // given
        File globalCache = IOUtils.mkdir(resource, 'globalCache')
        File projectGopath = IOUtils.mkdir(resource, 'projectGopath')
        when(cacheManager.getGlobalCachePath(anyString())).thenReturn(globalCache.toPath())
        when(resolvedDependency.getVersion()).thenReturn(revCommit.getName())
        when(gitAccessor.getRepository(globalCache)).thenReturn(repository)
        // when
        resolver.install(resolvedDependency, projectGopath)
        // then
        verify(gitAccessor).resetToCommit(repository, revCommit.getName())
    }

    @Test(expected = DependencyInstallationException)
    void 'exception in reset process should be wrapped'() {
        // given
        when(cacheManager.getGlobalCachePath(anyString())).thenThrow(new IllegalStateException())
        // then
        resolver.install(resolvedDependency, resource)
    }
}
