package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VendorNotationDependencyTest {
    @Mock
    AbstractNotationDependency hostNotationDependency
    @Mock
    ResolvedDependency hostResolvedDependency
    @Mock
    ResolveContext context

    VendorNotationDependency dependency = new VendorNotationDependency()

    @Before
    void setUp() {
        dependency.hostNotationDependency = hostNotationDependency
    }

    @Test
    void 'setting hostDependency should success'() {
        assert dependency.hostNotationDependency.is(hostNotationDependency)
    }

    @Test
    void 'setting vendorPath should success'() {
        dependency.vendorPath = 'vendor/a/b'
        assert dependency.vendorPath == 'vendor/a/b'
    }

    @Test
    void 'isConcrete should be delegated to hostDependency'() {
        when(hostNotationDependency.getCacheScope()).thenReturn(CacheScope.BUILD)
        assert dependency.getCacheScope() == CacheScope.BUILD
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be thrown if it does not exist in host\'s dependencies'() {
        // given
        when(hostNotationDependency.resolve(any(ResolveContext))).thenReturn(hostResolvedDependency)
        when(hostResolvedDependency.getDependencies()).thenReturn(GolangDependencySet.empty())
        // then
        dependency.resolve(context)
    }

    @Test
    void 'vendor resolved dependency should be picked up from host\'s descendants'() {
        // given
        when(hostNotationDependency.resolve(any(ResolveContext))).thenReturn(hostResolvedDependency)
        VendorResolvedDependencyForTest vendor1 = new VendorResolvedDependencyForTest('vendor1', 'version', 1L, hostResolvedDependency, 'vendor/vendor1')
        VendorResolvedDependencyForTest vendor2 = new VendorResolvedDependencyForTest('vendor2', 'version', 2L, hostResolvedDependency, 'vendor/vendor1/vendor/vendor2')
        vendor1.dependencies.add(vendor2)
        when(hostResolvedDependency.getDependencies()).thenReturn(DependencyUtils.asGolangDependencySet(vendor1))
        dependency.vendorPath = 'vendor/vendor1/vendor/vendor2'
        // then
        assert dependency.resolve(context).is(vendor2)
    }

    @Test
    void 'equals should succeed'() {
        assert !dependency.equals(null)
        assert vendorNotationDependency(hostNotationDependency, 'path1') == vendorNotationDependency(hostNotationDependency, 'path1')
        assert vendorNotationDependency(hostNotationDependency, 'path1') != vendorNotationDependency(hostNotationDependency, 'path2')
        assert vendorNotationDependency(hostNotationDependency, 'path1') != vendorNotationDependency(mock(NotationDependency), 'path1')
    }

    VendorNotationDependency vendorNotationDependency(NotationDependency host, String vendorPath) {
        VendorNotationDependency ret = new VendorNotationDependency()
        ret.vendorPath = vendorPath
        ret.hostNotationDependency = host
        return ret
    }
}
