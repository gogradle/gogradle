/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.exceptions.DependencyInstallationException
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.support.MockOffline
import com.github.blindpirate.gogradle.support.MockRefreshDependencies
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import com.github.blindpirate.gogradle.vcs.git.GitResolvedDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.concurrent.Callable
import java.util.function.Function

import static com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManagerTest.APPLY_FUNCTION_ANSWER
import static com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManagerTest.CALL_CALLABLE_ANSWER
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockWithName
import static java.util.Optional.empty
import static java.util.Optional.of
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class GitMercurialDependencyManagerTest {

    GitMercurialNotationDependency notationDependency = mockWithName(GitMercurialNotationDependency, 'github.com/a/b')
    VcsResolvedDependency resolvedDependency = mockWithName(VcsResolvedDependency, 'github.com/a/b')

    String DEFAULT_BRANCH = 'DEFAULT_BRANCH'

    @Mock
    GlobalCacheManager cacheManager
    @Mock
    GitMercurialAccessor accessor
    @Mock
    GolangDependencySet dependencySet
    @Mock
    GolangConfiguration configuration
    @Mock
    DependencyRegistry dependencyRegistry
    @Mock
    GitMercurialCommit commit
    @Mock
    ResolveContext resolveContext
    @Mock
    ProjectCacheManager projectCacheManager

    GitMercurialDependencyManager manager

    String commitId = '1' * 40
    String repoUrl = 'https://github.com/a/b.git'
    GolangPackage thePackage = VcsGolangPackage.builder()
            .withPath('github.com/a/b')
            .withRootPath('github.com/a/b')
            .withOriginalVcsInfo(VcsType.GIT, [repoUrl])
            .build()

    File resource

    @Before
    void setUp() {
        // prevent ensureGlobalCacheEmptyOrMatch from returning directly
        IOUtils.write(resource, '.git', '')

        manager = new GitMercurialDependencyManagerForTest(cacheManager, projectCacheManager, accessor)
        when(projectCacheManager.resolve(any(NotationDependency), any(Function))).thenAnswer(APPLY_FUNCTION_ANSWER)

        when(configuration.getDependencyRegistry()).thenReturn(dependencyRegistry)

        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable))).thenAnswer(CALL_CALLABLE_ANSWER)
        when(cacheManager.getGlobalPackageCachePath(anyString())).thenReturn(resource.toPath())
        when(accessor.findCommit(resource, commitId)).thenReturn(of(commit))
        when(accessor.headCommitOfBranch(resource, 'MockDefault')).thenReturn(commit)
        when(commit.getId()).thenReturn(commitId)
        when(commit.getCommitTime()).thenReturn(123000L)

        when(accessor.getRemoteUrl(resource)).thenReturn(repoUrl)
        when(accessor.getDefaultBranch(resource)).thenReturn(DEFAULT_BRANCH)

        when(notationDependency.getUrls()).thenReturn([repoUrl])
        when(notationDependency.getCommit()).thenReturn(commitId)
        when(notationDependency.getCacheScope()).thenReturn(CacheScope.PERSISTENCE)
        when(notationDependency.getPackage()).thenReturn(thePackage)

        when(resolveContext.getDependencyRegistry()).thenReturn(mock(DependencyRegistry))
    }

    @Test
    void 'notation dependency should be created successfully'() {
        // given
        when(notationDependency.getTag()).thenReturn('tag')
        when(notationDependency.isFirstLevel()).thenReturn(true)
        // when
        ResolvedDependency result = manager.createResolvedDependency(notationDependency, resource, commit, resolveContext)
        // then
        verify(resolveContext).produceTransitiveDependencies(result, resource)
        assertResolvedDependency(result)
    }

    void assertResolvedDependency(ResolvedDependency result) {
        assert result.name == 'github.com/a/b'
        assert ReflectionUtils.getField(result, 'url') == repoUrl
        assert ReflectionUtils.getField(result, 'tag') == 'tag'
        assert result.version == commitId
        assert result.updateTime == 123000L
        assert result.firstLevel
    }

    @Test
    @MockRefreshDependencies(false)
    void 'existed repository should not be updated if no --refresh-dependencies'() {
        // given:
        when(cacheManager.currentRepositoryIsUpToDate(notationDependency)).thenReturn(false)
        // when:
        manager.resolve(resolveContext, notationDependency)
        // then:
        verify(accessor, times(0)).update(resource)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown if commit not found'() {
        // given
        when(accessor.findCommit(resource, commitId)).thenReturn(Optional.empty())
        // then
        manager.determineVersion(resource, notationDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown if tag not found'() {
        // given
        when(notationDependency.getTag()).thenReturn('tag')
        // then
        manager.determineVersion(resource, notationDependency)
    }

    @Test
    @MockRefreshDependencies(true)
    @MockOffline(false)
    void 'existed repository should be updated if --refresh-dependencies'() {
        // given:
        when(cacheManager.currentRepositoryIsUpToDate(notationDependency)).thenReturn(false)
        // when:
        manager.resolve(resolveContext, notationDependency)
        // then:
        verify(accessor).update(resource)
    }

    @Test
    void 'empty repository should be cloned'() {
        // given
        IOUtils.clearDirectory(resource)
        // when
        manager.resolve(resolveContext, notationDependency)
        // then
        verify(accessor).clone(repoUrl, resource)
    }

    @Test
    @MockOffline(true)
    void 'pull should not be executed if offline'() {
        // when:
        manager.resolve(resolveContext, notationDependency)
        // then:
        verify(accessor, times(0)).update(resource)
    }

    @Test
    @MockRefreshDependencies(false)
    void 'dependency with tag should be resolved successfully'() {
        // given
        when(notationDependency.getTag()).thenReturn('tag')
        when(accessor.findCommitByTagOrBranch(resource, 'tag')).thenReturn(of(commit))
        // when
        manager.resolve(resolveContext, notationDependency)
        // then
        verify(accessor).checkout(resource, commitId)
    }

    @Test
    void 'tag should be interpreted as sem version if commit not found'() {
        // given
        when(notationDependency.getTag()).thenReturn('~1.0.0')
        when(accessor.findCommitByTagOrBranch(resource, '~1.0.0')).thenReturn(empty())
        GitMercurialCommit satisfiedCommit = GitMercurialCommit.of('commitId', '1.0.1', 321L)
        when(accessor.getAllTags(resource)).thenReturn([satisfiedCommit])
        // then
        assert manager.determineVersion(resource, notationDependency).is(satisfiedCommit)
    }

    @Test
    void 'commit will be searched if tag cannot be recognized'() {
        // given
        when(notationDependency.isLatest()).thenReturn(true)
        // when
        manager.determineVersion(resource, notationDependency)
        // then
        verify(accessor).headCommitOfBranch(resource, DEFAULT_BRANCH)
    }

    @Test
    void 'LATEST_COMMIT should be recognized properly'() {
        // given
        when(notationDependency.isLatest()).thenReturn(true)
        // when
        manager.determineVersion(resource, notationDependency)
        // then
        verify(accessor).headCommitOfBranch(resource, DEFAULT_BRANCH)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown when every url has been tried'() {
        // given
        when(accessor.clone('url1', resource)).thenThrow(new IllegalStateException())
        when(accessor.clone('url2', resource)).thenThrow(new IllegalStateException())
        // when
        manager.initRepository('notationDependency', ['url1', 'url2'], resource)
    }

    @Test
    void 'every url should be tried until success'() {
        // given
        when(notationDependency.getUrls()).thenReturn(['url1', 'url2'])
        when(accessor.clone('url1', resource)).thenThrow(IOException)
        // when
        manager.resolve(resolveContext, notationDependency)
        // then
        verify(accessor).clone('url2', resource)
    }

    @Test
    void 'resetting to a commit should succeed'() {
        // when
        manager.resetToSpecificVersion(resource, commit)
        // then
        verify(accessor).checkout(resource, commitId)
    }

    @Test(expected = DependencyResolutionException)
    void 'trying to resolve an inexistent commit should result in an exception'() {
        // given
        when(notationDependency.getCommit()).thenReturn('inexistent')
        // when
        manager.resolve(resolveContext, notationDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'trying to resolve an inexistent tag should result in an exception'() {
        // given
        when(notationDependency.getTag()).thenReturn('inexistent')
        // when
        manager.resolve(resolveContext, notationDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception in locked block should not be swallowed'() {
        // given
        when(cacheManager.runWithGlobalCacheLock(any(GitMercurialNotationDependency), any(Callable)))
                .thenThrow(new IOException())
        // when
        manager.resolve(resolveContext, notationDependency)
    }

    @Test
    void 'version existence in repository should be determined correctly'() {
        // given
        GitNotationDependency dependencyWithCommit = new GitNotationDependency()
        dependencyWithCommit.name = 'git'
        dependencyWithCommit.commit = 'commit'

        GitNotationDependency dependencyWithTag = new GitNotationDependency()
        dependencyWithTag.name = 'git'
        dependencyWithTag.tag = 'tag'

        GitResolvedDependency resolvedDependency = VcsResolvedDependency.builder(VcsType.GIT)
                .withNotationDependency(dependencyWithCommit)
                .withCommitId(dependencyWithCommit.commit)
                .build()
        when(accessor.findCommit(resource, 'commit')).thenReturn(Optional.of(commit))
        when(accessor.findCommitByTagOrBranch(resource, 'tag')).thenReturn(Optional.of(commit))
        // then
        assert manager.versionExistsInRepo(resource, dependencyWithCommit)
        assert manager.versionExistsInRepo(resource, dependencyWithTag)
        assert manager.versionExistsInRepo(resource, resolvedDependency)
    }


    @Test
    void 'mismatched repository should be cleared'() {
        // given
        when(notationDependency.getUrls()).thenReturn(['anotherUrl'])
        IOUtils.write(resource, 'some file', 'file content')
        // when
        manager.resolve(resolveContext, notationDependency)
        // then
        assert IOUtils.dirIsEmpty(resource)
    }

    @Test
    void 'installing a resolved dependency should succeed'() {
        // given
        File globalCache = IOUtils.mkdir(resource, 'globalCache')
        File projectGopath = IOUtils.mkdir(resource, 'projectGopath')
        when(cacheManager.getGlobalPackageCachePath(anyString())).thenReturn(globalCache.toPath())
        when(resolvedDependency.getVersion()).thenReturn(commitId)
        // when
        manager.install(resolvedDependency, projectGopath)
        // then
        verify(accessor).checkout(globalCache, commitId)
    }

    @Test(expected = DependencyInstallationException)
    void 'exception in install process should be wrapped'() {
        // given
        when(cacheManager.getGlobalPackageCachePath(anyString())).thenThrow(IllegalStateException)
        // then
        manager.install(resolvedDependency, resource)
    }

    class GitMercurialDependencyManagerForTest extends GitMercurialDependencyManager {
        GitMercurialAccessor accessor

        GitMercurialDependencyManagerForTest(GlobalCacheManager globalCacheManager,
                                             ProjectCacheManager projectCacheManager,
                                             GitMercurialAccessor accessor) {
            super(globalCacheManager, projectCacheManager)
            this.accessor = accessor
        }

        @Override
        protected GitMercurialAccessor getAccessor() {
            return accessor
        }

        @Override
        protected VcsType getVcsType() {
            return VcsType.GIT
        }
    }

    @Test
    void 'finding commit by sem version expression should succeed'() {
        // when
        def tags = ['3.0.0', '2.1.2', '2.1.1', '2.1.0', '2.0', '1.2.0', '1.0.0', '0.0.3-prerelease', 'v0.0.2', '0.0.1'].collect {
            GitMercurialCommit.of('commit', it, 0L)
        }
        when(accessor.getAllTags(resource)).thenReturn(tags)
        //3.0.0
        assert findMatchedTag('3.x') == '3.0.0'
        // NOT 1.0.0
        assert findMatchedTag('!(1.0.0)') == '3.0.0'

        // 3.0.0
        assert findMatchedTag('2.0-3.0') == '3.0.0'

        // 2.1.2
        assert findMatchedTag('~2.1.0') == '2.1.2'
        // 1.2.0
        assert findMatchedTag('>=1.0.0 & <2.0.0') == '1.2.0'
    }

    def findMatchedTag(String expression) {
        when(notationDependency.getTag()).thenReturn(expression)
        return manager.determineVersion(resource, notationDependency).tag
    }

}
