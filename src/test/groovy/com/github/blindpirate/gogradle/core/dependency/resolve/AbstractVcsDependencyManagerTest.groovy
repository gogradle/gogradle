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
import static org.mockito.Mockito.when

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

    File resource
    File src
    File dest


    @Before
    void setUp() {
        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable))).thenAnswer(callCallableAnswer)
        manager = new TestAbstractVcsDependencyManager(cacheManager)

        when(vendorResolvedDependency.getHostDependency()).thenReturn(hostResolvedDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn(Paths.get('vendor/root/package'))

        when(vendorNotationDependency.getName()).thenReturn('thisisvendor')
        when(vendorResolvedDependency.getName()).thenReturn('thisisvendor')
        when(cacheManager.getGlobalCachePath('thisisvendor')).thenReturn(resource.toPath())
    }

    @Test
    void 'installing a vendor dependency hosting in vcs dependency should succeed'() {
        // given
        src = IOUtils.mkdir(resource, 'src')
        dest = IOUtils.mkdir(resource, 'dest')
        IOUtils.write(src, 'vendor/root/package/main.go', 'This is main.go')
        when(hostResolvedDependency.getName()).thenReturn('host')
        when(cacheManager.getGlobalCachePath('host')).thenReturn(src.toPath())
        // when
        manager.install(vendorResolvedDependency, dest)
        // then
        assert dest.toPath().resolve('main.go').toFile().getText() == 'This is main.go'
    }

    @Test
    void 'resolving a vendor dependency hosting in vcs dependency should succeed'() {
        // given
        GolangDependencySet set = DependencyUtils.asGolangDependencySet(vendorResolvedDependency)
        when(hostResolvedDependency.getDependencies()).thenReturn(set)
        when(vendorResolvedDependency.getDependencies()).thenReturn(GolangDependencySet.empty())
        when(vendorNotationDependency.getVendorPath()).thenReturn('vendor/root/package')

        // when
        manager.resolve(vendorNotationDependency)
        // then
        assert manager.resolve(vendorNotationDependency).is(vendorResolvedDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown if the result does not exist in transitive dependencies of host dependency'() {
        // given
        when(hostResolvedDependency.getDependencies()).thenReturn(GolangDependencySet.empty())
        // then
        manager.resolve(vendorNotationDependency)
    }


    class TestAbstractVcsDependencyManager extends AbstractVcsDependencyManager {

        TestAbstractVcsDependencyManager(GlobalCacheManager cacheManager) {
            super(cacheManager)
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
