package com.github.blindpirate.gogradle.vcs.mercurial

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Paths
import java.util.concurrent.Callable

import static com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManagerTest.callCallableAnswer
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockWithName
import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class MercurialDependencyManagerTest {
    @Mock
    MercurialAccessor mercurialAccessor
    @Mock
    DependencyVisitor visitor
    @Mock
    GlobalCacheManager cacheManager
    @Mock
    DependencyRegistry dependencyRegistry
    @Mock
    HgChangeset hgChangeset
    @Mock
    GolangDependencySet dependencySet
    @Mock
    DependencyProduceStrategy strategy
    @Mock
    HgRepository repository

    MercurialNotationDependency notationDependency = mockWithName(MercurialNotationDependency, 'bitbucket.org/user/project')
    MercurialResolvedDependency resolvedDependency = mockWithName(MercurialResolvedDependency, 'bitbucket.org/user/project')

    MercurialDependencyManager manager


    File resource

    String repoUrl = 'https://user@bitbucket.org/user/project'
    GolangPackage pkg = VcsGolangPackage.builder()
            .withPath('bitbucket.org/user/project')
            .withRootPath('bitbucket.org/user/project')
            .withVcsType(VcsType.MERCURIAL)
            .withUrl(repoUrl)
            .build()

    @Before
    void setUp() {
        manager = new MercurialDependencyManager(mercurialAccessor, visitor, cacheManager, dependencyRegistry)

        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable))).thenAnswer(callCallableAnswer)
        when(cacheManager.getGlobalPackageCachePath(anyString())).thenReturn(resource.toPath())

        when(mercurialAccessor.getRepository(any(File))).thenReturn(repository)

        when(resolvedDependency.getVersion()).thenReturn('nodeId')

        when(notationDependency.getPackage()).thenReturn(pkg)
        when(notationDependency.getTag()).thenReturn('tag')
        when(notationDependency.getNodeId()).thenReturn('nodeId')
        when(notationDependency.getStrategy()).thenReturn(strategy)

        when(strategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor))).thenReturn(dependencySet)

        when(dependencySet.flatten()).thenReturn([])

        when(hgChangeset.getNodeid()).thenReturn('1' * 40)
        when(hgChangeset.getCommitTime()).thenReturn(1L)
    }

    @Test
    void 'installing mercurial dependency should succeed'() {
        // when
        manager.doReset(resolvedDependency, resource.toPath())
        // then
        verify(mercurialAccessor).resetToSpecificNodeId(repository, 'nodeId')
    }

    @Test
    void 'creating resolved dependency should succeed'() {
        // given
        VendorResolvedDependency vendorResolvedDependency = mockWithName(VendorResolvedDependency, 'vendor')
        when(dependencySet.flatten()).thenReturn([vendorResolvedDependency])
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn(Paths.get('relative/path'))
        // when
        ResolvedDependency result = manager.createResolvedDependency(notationDependency, resource, repository, hgChangeset)
        // then
        assert result instanceof MercurialResolvedDependency
        assert result.tag == 'tag'
        assert result.name == 'bitbucket.org/user/project'
        assert result.version == '1' * 40
        assert result.updateTime == 1L
        verify(mercurialAccessor).getLastCommitTimeOfPath(repository, 'relative/path')
    }

    @Test
    void 'tag should be used if it exists'() {
        // given
        when(mercurialAccessor.findChangesetByTag(repository, 'tag')).thenReturn(of(hgChangeset))
        // then
        assert manager.determineVersion(repository, notationDependency) == hgChangeset
    }

    @Test
    void 'nodeId should be used if it exists'() {
        // given
        when(mercurialAccessor.findChangesetById(repository, 'nodeId')).thenReturn(of(hgChangeset))
        // then
        assert manager.determineVersion(repository, notationDependency) == hgChangeset
    }

    @Test
    void 'head commit should be used if tag and nodeId do not exist'() {
        // given
        when(mercurialAccessor.headOfBranch(repository, 'default')).thenReturn(hgChangeset)
        // then
        assert manager.determineVersion(repository, notationDependency) == hgChangeset
    }

    @Test
    void 'head commit should be used if nodeId is NEWEST_COMMIT'() {
        // given
        when(notationDependency.getNodeId()).thenReturn('NEWEST_COMMIT')
        when(mercurialAccessor.headOfBranch(repository, 'default')).thenReturn(hgChangeset)
        // then
        assert manager.determineVersion(repository, notationDependency) == hgChangeset
    }

    @Test
    void 'repository should be updated successfully'() {
        // when
        manager.updateRepository(notationDependency, repository, resource)
        // then
        verify(mercurialAccessor).pull(repository)
    }

    @Test
    void 'all urls should be tried to clone repo'() {
        // given
        when(notationDependency.getUrls()).thenReturn(['url1', 'url2'])
        when(mercurialAccessor.cloneWithUrl(resource, 'url1')).thenThrow(new IllegalStateException())
        // when
        manager.initRepository(notationDependency, resource)
        // then
        verify(mercurialAccessor).cloneWithUrl(resource, 'url2')
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown if all urls are tried'() {
        // when
        when(notationDependency.getUrls()).thenReturn(['url1', 'url2'])
        when(mercurialAccessor.cloneWithUrl(any(File), anyString())).thenThrow(new IllegalStateException())
        // then
        manager.initRepository(notationDependency, resource)
    }

    @Test
    void 'repo should be considered matched if url matched'() {
        // given
        when(mercurialAccessor.getRemoteUrl(resource)).thenReturn('url')
        when(notationDependency.getUrls()).thenReturn(['url', 'url2'])
        // then
        assert manager.repositoryMatch(resource, notationDependency).isPresent()
    }

    @Test
    void 'repo should be considered unmatched if url not matched'() {
        // given
        when(mercurialAccessor.getRemoteUrl(repository)).thenReturn('url')
        when(notationDependency.getUrls()).thenReturn(['url1', 'url2'])
        // then
        assert !manager.repositoryMatch(resource, notationDependency).isPresent()
    }
}
