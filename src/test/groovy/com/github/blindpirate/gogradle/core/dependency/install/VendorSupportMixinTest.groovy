package com.github.blindpirate.gogradle.core.dependency.install

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import org.junit.Before
import org.junit.Test

import static com.github.blindpirate.gogradle.util.DependencyUtils.mockWithName
import static org.mockito.Mockito.when

class VendorSupportMixinTest {
    ResolvedDependency resolvedDependency = mockWithName(ResolvedDependency, 'resolved')
    ResolvedDependency hostDependency = mockWithName(ResolvedDependency, 'host')
    VendorResolvedDependency vendorResolvedDependency = mockWithName(VendorResolvedDependency, 'vendor')

    VendorSupportMixin mixin = new VendorSupportMixin() {
    }

    @Before
    void setUp() {
        when(vendorResolvedDependency.getHostDependency()).thenReturn(hostDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn('vendor/github.com/a/b')
    }

    @Test
    void 'determining real path should succeed'() {
        assert mixin.determineDependency(resolvedDependency).is(resolvedDependency)
        assert mixin.determineDependency(vendorResolvedDependency).is(hostDependency)
    }

    @Test
    void 'determining relative path to host should succeed'() {
        assert mixin.determineRelativePath(vendorResolvedDependency) == 'vendor/github.com/a/b'
        assert mixin.determineRelativePath(resolvedDependency) == '.'
    }
}
