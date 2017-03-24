package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.support.MockOffline
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class AbstractVcsDependencyManagerTest {

    public static final Answer callCallableAnswer = new Answer() {
        @Override
        Object answer(InvocationOnMock invocation) throws Throwable {
            return invocation.getArgument(1).call()
        }
    }

    AbstractVcsDependencyManager manager
    @Mock
    AbstractVcsDependencyManager subclassDelegate
    @Mock
    GlobalCacheManager cacheManager
    @Mock
    VendorResolvedDependency vendorResolvedDependency
    @Mock
    VendorNotationDependency vendorNotationDependency
    @Mock
    DependencyRegistry dependencyRegistry
    @Mock
    GolangConfiguration configuration


    ResolvedDependency hostResolvedDependency = DependencyUtils.mockWithName(ResolvedDependency, 'host')
    NotationDependency hostNotationDependency = DependencyUtils.mockWithName(NotationDependency, 'host')

    File resource

    @Before
    void setUp() {
        // prevent ensureGlobalCacheEmptyOrMatch from returning directly
        IOUtils.write(resource, '.git', '')

        when(configuration.getDependencyRegistry()).thenReturn(dependencyRegistry)
        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable))).thenAnswer(callCallableAnswer)
        manager = new TestAbstractVcsDependencyManager(cacheManager)

        when(subclassDelegate.determineVersion(resource, hostNotationDependency)).thenReturn('version')
        when(subclassDelegate.createResolvedDependency(hostNotationDependency, resource, 'version')).thenReturn(hostResolvedDependency)
        when(subclassDelegate.repositoryMatch(resource, hostNotationDependency)).thenReturn(true)

        when(vendorResolvedDependency.getHostDependency()).thenReturn(hostResolvedDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn(Paths.get('vendor/root/package'))

        when(vendorNotationDependency.getHostNotationDependency()).thenReturn(hostNotationDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn(Paths.get('vendor/root/package'))

        when(vendorNotationDependency.getName()).thenReturn('thisisvendor')
        when(vendorResolvedDependency.getName()).thenReturn('thisisvendor')
        when(cacheManager.getGlobalPackageCachePath('host')).thenReturn(resource.toPath())
    }

    @Test
    void 'installing a vendor dependency hosting in vcs dependency should succeed'() {
        // given
        File src = IOUtils.mkdir(resource, 'src')
        File dest = IOUtils.mkdir(resource, 'dest')
        IOUtils.write(src, 'vendor/root/package/main.go', 'This is main.go')
        when(hostResolvedDependency.getName()).thenReturn('host')
        when(cacheManager.getGlobalPackageCachePath('host')).thenReturn(src.toPath())
        // when
        manager.install(vendorResolvedDependency, dest)
        // then
        assert new File(dest, 'main.go').getText() == 'This is main.go'
    }

    @Test
    void 'resolving a vendor dependency hosting in vcs dependency should succeed'() {
        // given
        GolangDependencySet set = asGolangDependencySet(vendorResolvedDependency)
        when(hostResolvedDependency.getDependencies()).thenReturn(set)
        when(vendorResolvedDependency.getDependencies()).thenReturn(GolangDependencySet.empty())
        when(vendorNotationDependency.getVendorPath()).thenReturn('vendor/root/package')

        // when
        ResolvedDependency result = manager.resolve(configuration, vendorNotationDependency)
        // then
        assert result.is(vendorResolvedDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown if the result does not exist in transitive dependencies of host dependency'() {
        // given
        when(hostResolvedDependency.getDependencies()).thenReturn(GolangDependencySet.empty())
        // then
        manager.resolve(configuration, vendorNotationDependency)
    }

    @Test
    void 'result in cache should be fetched'() {
        // given
        when(dependencyRegistry.getFromCache(vendorNotationDependency))
                .thenReturn(Optional.of(vendorResolvedDependency))
        // then
        assert manager.resolve(configuration, vendorNotationDependency).is(vendorResolvedDependency)
        verify(cacheManager, times(0)).runWithGlobalCacheLock(any(GolangDependency), any(Callable))
    }

    @Test
    void 'result should be put into cache after resolving'() {
        'resolving a vendor dependency hosting in vcs dependency should succeed'()
        verify(dependencyRegistry).putIntoCache(vendorNotationDependency, vendorResolvedDependency)
    }

    @Test
    void 'updating repository should be skipped if it is up-to-date'() {
        'resolving a vendor dependency hosting in vcs dependency should succeed'()
        verify(cacheManager, times(0)).updateCurrentDependencyLock()
        verify(subclassDelegate, times(0)).updateRepository(hostNotationDependency, resource)
    }

    @Test
    @MockOffline
    void 'updating repository should be skipped when offline'() {
        // given
        when(cacheManager.currentDependencyIsOutOfDate()).thenReturn(true)
        'resolving a vendor dependency hosting in vcs dependency should succeed'()
        verify(cacheManager, times(0)).updateCurrentDependencyLock()
        verify(subclassDelegate, times(0)).updateRepository(hostNotationDependency, resource)
    }

    @Test
    void 'lock file should be updated after resolving'() {
        // given
        when(cacheManager.currentDependencyIsOutOfDate(hostNotationDependency)).thenReturn(true)
        // when
        'resolving a vendor dependency hosting in vcs dependency should succeed'()
        // then
        verify(subclassDelegate).updateRepository(hostNotationDependency, resource)
        verify(cacheManager).updateCurrentDependencyLock(hostNotationDependency)
    }

    @Test
    void 'lock file should be updated after repository being initialized'() {
        // given
        when(subclassDelegate.repositoryMatch(resource, hostNotationDependency)).thenReturn(false)
        // when
        manager.resolve(configuration, hostNotationDependency)
        // then
        verify(cacheManager).updateCurrentDependencyLock(hostNotationDependency)
    }


    class TestAbstractVcsDependencyManager extends AbstractVcsDependencyManager {
        TestAbstractVcsDependencyManager(GlobalCacheManager cacheManager) {
            super(cacheManager)
        }

        @Override
        protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
            subclassDelegate.doReset(dependency, globalCachePath)
        }

        @Override
        protected ResolvedDependency createResolvedDependency(NotationDependency dependency, File repoRoot, Object o) {
            return subclassDelegate.createResolvedDependency(dependency, repoRoot, o)
        }

        @Override
        protected void resetToSpecificVersion(File repository, Object o) {
            subclassDelegate.resetToSpecificVersion(repository, o)
        }

        @Override
        protected Object determineVersion(File repository, NotationDependency dependency) {
            return subclassDelegate.determineVersion(repository, dependency)
        }

        @Override
        protected void updateRepository(NotationDependency dependency, File repoRoot) {
            subclassDelegate.updateRepository(dependency, repoRoot)
        }

        @Override
        protected void initRepository(NotationDependency dependency, File repoRoot) {
            subclassDelegate.initRepository(dependency, repoRoot)
        }

        @Override
        protected boolean repositoryMatch(File directory, NotationDependency dependency) {
            return subclassDelegate.repositoryMatch(directory, dependency)
        }
    }
}
