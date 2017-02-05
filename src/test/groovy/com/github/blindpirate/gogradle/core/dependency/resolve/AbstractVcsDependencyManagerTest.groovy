package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
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


    Object repository = new Object()
    ResolvedDependency hostResolvedDependency = DependencyUtils.mockWithName(ResolvedDependency, 'host')
    NotationDependency hostNotationDependency = DependencyUtils.mockWithName(NotationDependency, 'host')

    File resource

    @Before
    void setUp() {
        // prevent ensureGlobalCacheEmptyOrMatch from returning directly
        IOUtils.write(resource, '.git', '')

        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable))).thenAnswer(callCallableAnswer)
        manager = new TestAbstractVcsDependencyManager(cacheManager, dependencyRegistry)

        when(subclassDelegate.determineVersion(repository, hostNotationDependency)).thenReturn('version')
        when(subclassDelegate.createResolvedDependency(hostNotationDependency, resource, repository, 'version')).thenReturn(hostResolvedDependency)
        when(subclassDelegate.repositoryMatch(resource, hostNotationDependency)).thenReturn(Optional.of(repository))

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
        ResolvedDependency result = manager.resolve(vendorNotationDependency)
        // then
        assert result.is(vendorResolvedDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown if the result does not exist in transitive dependencies of host dependency'() {
        // given
        when(hostResolvedDependency.getDependencies()).thenReturn(GolangDependencySet.empty())
        // then
        manager.resolve(vendorNotationDependency)
    }

    @Test
    void 'result in cache should be fetched'() {
        // given
        when(dependencyRegistry.getFromCache(vendorNotationDependency))
                .thenReturn(Optional.of(vendorResolvedDependency))
        // then
        assert manager.resolve(vendorNotationDependency).is(vendorResolvedDependency)
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
        verify(subclassDelegate, times(0)).updateRepository(hostNotationDependency, repository, resource)
    }

    @Test
    void 'lock file should be updated after resolving'() {
        // given
        when(cacheManager.currentDependencyIsOutOfDate()).thenReturn(true)
        // when
        'resolving a vendor dependency hosting in vcs dependency should succeed'()
        // then
        verify(subclassDelegate).updateRepository(hostNotationDependency, repository, resource)
        verify(cacheManager).updateCurrentDependencyLock()
    }


    class TestAbstractVcsDependencyManager extends AbstractVcsDependencyManager {

        TestAbstractVcsDependencyManager(GlobalCacheManager cacheManager,
                                         DependencyRegistry dependencyRegistry) {
            super(cacheManager, dependencyRegistry)
        }

        @Override
        protected ResolvedDependency createResolvedDependency(NotationDependency dependency, File directory, Object o, Object o2) {
            return subclassDelegate.createResolvedDependency(dependency, directory, o, o2)
        }

        @Override
        protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
            subclassDelegate.doReset(dependency, globalCachePath)
        }


        @Override
        protected void resetToSpecificVersion(Object o, Object o2) {
            subclassDelegate.resetToSpecificVersion(o, o2)
        }

        @Override
        protected Object determineVersion(Object o, NotationDependency dependency) {
            return subclassDelegate.determineVersion(o, dependency)
        }

        @Override
        protected Object updateRepository(NotationDependency dependency, Object o, File directory) {
            return subclassDelegate.updateRepository(dependency, o, directory)
        }

        @Override
        protected Object initRepository(NotationDependency dependency, File directory) {
            return subclassDelegate.initRepository(dependency, directory)
        }

        @Override
        protected Optional repositoryMatch(File directory, NotationDependency dependency) {
            return subclassDelegate.repositoryMatch(directory, dependency)
        }
    }
}
