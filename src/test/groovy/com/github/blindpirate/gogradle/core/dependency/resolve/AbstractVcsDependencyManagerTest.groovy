package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
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
    GlobalCacheManager cacheManager
    @Mock
    ResolvedDependency hostResolvedDependency
    @Mock
    VendorResolvedDependency vendorResolvedDependency
    @Mock
    VendorNotationDependency vendorNotationDependency
    @Mock
    DependencyRegistry dependencyRegistry

    File resource
    File src
    File dest


    @Before
    void setUp() {
        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable))).thenAnswer(callCallableAnswer)
        manager = new TestAbstractVcsDependencyManager(cacheManager, dependencyRegistry)

        when(vendorResolvedDependency.getHostDependency()).thenReturn(hostResolvedDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn(Paths.get('vendor/root/package'))

        when(vendorNotationDependency.getName()).thenReturn('thisisvendor')
        when(vendorResolvedDependency.getName()).thenReturn('thisisvendor')
        when(cacheManager.getGlobalPackageCachePath('thisisvendor')).thenReturn(resource.toPath())
    }

    @Test
    void 'installing a vendor dependency hosting in vcs dependency should succeed'() {
        // given
        src = IOUtils.mkdir(resource, 'src')
        dest = IOUtils.mkdir(resource, 'dest')
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
        GolangDependencySet set = DependencyUtils.asGolangDependencySet(vendorResolvedDependency)
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


    class TestAbstractVcsDependencyManager extends AbstractVcsDependencyManager {

        TestAbstractVcsDependencyManager(GlobalCacheManager cacheManager,
                                         DependencyRegistry dependencyRegistry) {
            super(cacheManager, dependencyRegistry)
        }

        @Override
        protected ResolvedDependency createResolvedDependency(NotationDependency dependency, File directory, Object o, Object o2) {
            return hostResolvedDependency
        }

        @Override
        protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
        }


        @Override
        protected void resetToSpecificVersion(Object o, Object o2) {
        }

        @Override
        protected Object determineVersion(Object o, NotationDependency dependency) {
            return null
        }

        @Override
        protected Object updateRepository(Object o, File directory) {
            return null
        }

        @Override
        protected Object initRepository(NotationDependency dependency, File directory) {
            return null
        }

        @Override
        protected Optional repositoryMatch(File directory, NotationDependency dependency) {
            return null
        }
    }
}
