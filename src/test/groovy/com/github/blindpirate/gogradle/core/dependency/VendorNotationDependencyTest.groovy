package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VendorNotationDependencyTest {
    @Mock
    AbstractNotationDependency hostNotationDependency

    VendorNotationDependency dependency = new VendorNotationDependency()

    @Before
    void setUp() {
        when(hostNotationDependency.getResolverClass()).thenReturn(GitDependencyManager)
    }

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
    void "a VendorNotationDependency's resolver class should be its host's"() {
        dependency.hostNotationDependency = hostNotationDependency
        assert dependency.resolverClass == GitDependencyManager
    }

    @Test
    void 'isConcrete should be delegated to hostDependency'() {
        when(hostNotationDependency.isConcrete()).thenReturn(true)
        assert dependency.isConcrete()
    }
}
