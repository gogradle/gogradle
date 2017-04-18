package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VendorNotationDependencyTest {
    @Mock
    AbstractNotationDependency hostNotationDependency

    VendorNotationDependency dependency = new VendorNotationDependency()

    @Test
    void 'setting hostDependency should success'() {
        dependency.hostNotationDependency = hostNotationDependency
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
        dependency.hostNotationDependency = hostNotationDependency
        assert dependency.getCacheScope() == CacheScope.BUILD
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
