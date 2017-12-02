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

package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.support.MockOffline
import com.github.blindpirate.gogradle.support.MockRefreshDependencies
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.VcsNotationDependency
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.function.Function

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
@MockOffline(false)
class AbstractVcsDependencyManagerTest {
    public static final Answer APPLY_FUNCTION_ANSWER = new Answer() {
        @Override
        Object answer(InvocationOnMock invocation) throws Throwable {
            return invocation.getArgument(1).apply(invocation.getArgument(0))
        }
    }

    AbstractVcsDependencyManager manager
    @Mock
    AbstractVcsDependencyManager subclassDelegate
    @Mock
    GlobalCacheManager globalCacheManager
    @Mock
    VendorResolvedDependency vendorResolvedDependency
    @Mock
    DependencyRegistry dependencyRegistry
    @Mock
    ResolveContext context
    @Mock
    ProjectCacheManager projectCacheManager

    VcsResolvedDependency hostResolvedDependency = DependencyUtils.mockWithName(VcsResolvedDependency, 'vcs')
    VcsNotationDependency notationDependency = DependencyUtils.mockWithName(VcsNotationDependency, 'vcs')

    VcsGolangPackage pkg = MockUtils.mockRootVcsPackage()

    File resource

    File repoRoot

    File targetDir

    @Before
    void setUp() {

        repoRoot = IOUtils.mkdir(resource, 'repo')
        targetDir = IOUtils.mkdir(resource, 'target')
        // prevent ensureGlobalCacheEmptyOrMatch from returning directly
        IOUtils.write(repoRoot, 'vendor/root/package/main.go', 'This is main.go')

        when(context.getDependencyRegistry()).thenReturn(dependencyRegistry)

        when(projectCacheManager.resolve(any(NotationDependency), any(Function))).thenAnswer(APPLY_FUNCTION_ANSWER)
        manager = new AbstractVcsDependencyManagerForTest(globalCacheManager, projectCacheManager)

        when(subclassDelegate.determineVersion(repoRoot, notationDependency)).thenReturn('version')
        when(subclassDelegate.createResolvedDependency(notationDependency, repoRoot, 'version', context)).thenReturn(hostResolvedDependency)

        when(vendorResolvedDependency.getHostDependency()).thenReturn(hostResolvedDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn('vendor/root/package')

        when(notationDependency.getName()).thenReturn('vcs')
        when(vendorResolvedDependency.getName()).thenReturn('thisisvendor')
        when(globalCacheManager.getGlobalCacheRepoDir()).thenReturn(repoRoot)

        when(notationDependency.getPackage()).thenReturn(MockUtils.mockRootVcsPackage())

        when(hostResolvedDependency.getPackage()).thenReturn(pkg)

        [vendorResolvedDependency, hostResolvedDependency, notationDependency].each {
            when(it.getSubpackages()).thenReturn(['...'] as Set)
        }
    }

    @Test
    void 'getting projectCacheManager should succeed'() {
        assert manager.getProjectCacheManager()
    }

    @Test
    void 'installing a vendor dependency hosting in vcs dependency should succeed'() {
        // given
        when(hostResolvedDependency.getName()).thenReturn('vcs')
        // when
        manager.install(vendorResolvedDependency, targetDir)
        // then
        assert new File(targetDir, 'main.go').getText() == 'This is main.go'
        verify(subclassDelegate).updateRepository(hostResolvedDependency, repoRoot)
        verify(globalCacheManager).repoUpdated()
    }

    @Test
    void 'repository should not be updated if version exist'() {
        // given
        when(subclassDelegate.versionExistsInRepo(repoRoot, hostResolvedDependency)).thenReturn(true)
        // when
        manager.install(hostResolvedDependency, targetDir)
        // then
        verify(subclassDelegate, times(0)).updateRepository(hostResolvedDependency, repoRoot)
        verify(globalCacheManager, times(0)).repoUpdated()
    }

    @Test
    @MockRefreshDependencies(true)
    void 'resolving a vcs dependency should succeed'() {
        // when
        ResolvedDependency result = manager.resolve(context, notationDependency)
        // then
        assert result.is(hostResolvedDependency)
    }

    @Test
    @MockRefreshDependencies(false)
    void 'updating repository should be skipped if it is up-to-date'() {
        when(globalCacheManager.currentRepositoryIsUpToDate()).thenReturn(true)
        'resolving a vcs dependency should succeed'()
        verify(globalCacheManager, times(0)).repoUpdated()
        verify(subclassDelegate, times(0)).updateRepository(notationDependency, repoRoot)
    }

    @Test
    @MockOffline(true)
    void 'updating repository should be skipped when offline'() {
        // given
        when(globalCacheManager.currentRepositoryIsUpToDate()).thenReturn(false)
        'resolving a vcs dependency should succeed'()
        verify(globalCacheManager, times(0)).repoUpdated()
        verify(subclassDelegate, times(0)).updateRepository(notationDependency, repoRoot)
    }

    @Test
    void 'lock file should be updated after resolving'() {
        // given
        when(globalCacheManager.currentRepositoryIsUpToDate()).thenReturn(false)
        when(notationDependency.getCacheScope()).thenReturn(CacheScope.PERSISTENCE)
        // when
        'resolving a vcs dependency should succeed'()
        // then
        verify(subclassDelegate).updateRepository(notationDependency, repoRoot)
        verify(globalCacheManager).repoUpdated()
    }

    @Test
    void 'lock file should be updated after repository being initialized'() {
        // when
        manager.resolve(context, notationDependency)
        // then
        verify(globalCacheManager).repoUpdated()
    }

    @Test
    void 'host dependency should be locked when installing'() {
        // when
        manager.install(vendorResolvedDependency, targetDir)
        // then
        verify(globalCacheManager).startSession(pkg)
    }

    class AbstractVcsDependencyManagerForTest extends AbstractVcsDependencyManager {
        AbstractVcsDependencyManagerForTest(GlobalCacheManager globalCacheManager,
                                            ProjectCacheManager projectCacheManager) {
            super(globalCacheManager, projectCacheManager)
        }

        @Override
        protected void doReset(ResolvedDependency dependency, File globalCachePath) {
            subclassDelegate.doReset(dependency, globalCachePath)
        }

        @Override
        protected ResolvedDependency createResolvedDependency(NotationDependency dependency, File repoRoot, Object o, ResolveContext context) {
            return subclassDelegate.createResolvedDependency(dependency, repoRoot, o, context)
        }

        @Override
        protected void resetToSpecificVersion(File repository, Object o) {
            subclassDelegate.resetToSpecificVersion(repoRoot, o)
        }

        @Override
        protected Object determineVersion(File repository, NotationDependency dependency) {
            return subclassDelegate.determineVersion(repoRoot, dependency)
        }

        @Override
        protected boolean versionExistsInRepo(File repoRoot, GolangDependency dependency) {
            return subclassDelegate.versionExistsInRepo(repoRoot, dependency)
        }

        @Override
        protected void updateRepository(GolangDependency dependency, File repoRoot) {
            subclassDelegate.updateRepository(dependency, repoRoot)
        }

        @Override
        protected void initRepository(String name, List urls, File repoRoot) {
            subclassDelegate.initRepository(name, urls, repoRoot)
        }
    }
}
