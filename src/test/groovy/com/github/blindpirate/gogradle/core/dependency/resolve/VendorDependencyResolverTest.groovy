package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import com.github.blindpirate.gogradle.vcs.git.GitResolvedDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VendorDependencyResolverTest {
    VendorDependencyResolver resolver = new VendorDependencyResolver()

    @Mock
    GitNotationDependency hostDependency
    @Mock
    VendorNotationDependency vendorNotationDependency
    @Mock
    GitResolvedDependency gitResolvedDependency
    @Mock
    VendorResolvedDependency vendorResolvedDependency

    @Before
    void setUp() {
        when(vendorNotationDependency.getHostNotationDependency()).thenReturn(hostDependency)
        when(vendorNotationDependency.getVendorPath()).thenReturn('vendor/a/b')
        when(hostDependency.resolve()).thenReturn(gitResolvedDependency)
        when(gitResolvedDependency.getUpdateTime()).thenReturn(1L)
        when(gitResolvedDependency.getVersion()).thenReturn('commitId')
    }

    @Test
    void 'resolving a vendor dependency should succeed'() {
        // when
        VendorResolvedDependency result = resolver.resolve(vendorNotationDependency)
        // then
        assert result.getUpdateTime() == 1L
        assert result.getVersion() == 'commitId'
        assert result.getHostDependency() == gitResolvedDependency
        assert result.getRelativePathToHost().toString() == 'vendor/a/b'
    }

    File resource

//    @Test
//    @WithResource('')
//    void 'resetting a vendor dependency should succeed'() {
//        // given
//        File src = IOUtils.mkdir(resource, 'src')
//        File dest = IOUtils.mkdir(resource, 'dest')
//        IOUtils.write(src, 'main.go', 'This is main.go')
//        // when
//        resolver.reset(vendorResolvedDependency, dest)
//        // then
//        assert dest.toPath().resolve('main.go').toFile().getText() == 'This is main.go'
//    }

}
