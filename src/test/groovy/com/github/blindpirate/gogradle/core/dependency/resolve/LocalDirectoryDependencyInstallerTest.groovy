package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Paths

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class LocalDirectoryDependencyInstallerTest {

    File resource

    LocalDirectoryDependencyInstaller installer = new LocalDirectoryDependencyInstaller()

    @Mock
    LocalDirectoryDependency dependency
    @Mock
    VendorResolvedDependency vendorResolvedDependency

    File src
    File dest

    @Before
    void setUp() {
        src = IOUtils.mkdir(resource, 'src')
        dest = IOUtils.mkdir(resource, 'dest')
        when(dependency.getRootDir()).thenReturn(src)
    }

    @Test
    void 'installing a local dependency should succeed'() {
        // given
        IOUtils.write(src, 'main.go', 'This is main.go')
        // when
        installer.install(dependency, dest)
        // then
        assert new File(dest, 'main.go').getText() == 'This is main.go'
    }

    @Test
    void 'installing a dependency hosting in local dependency should succeed'() {
        // given
        when(vendorResolvedDependency.getHostDependency()).thenReturn(dependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn('vendor/root/package')
        IOUtils.write(src, 'vendor/root/package/main.go', 'This is main.go')
        // when
        installer.install(vendorResolvedDependency, dest)
        assert new File(dest, 'main.go').getText() == 'This is main.go'
    }
}
