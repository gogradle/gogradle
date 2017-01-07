package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class LocalDirectoryDependencyInstallerTest {

    File resource

    LocalDirectoryDependencyInstaller installer = new LocalDirectoryDependencyInstaller()

    @Mock
    LocalDirectoryDependency dependency


    @Test
    void 'resetting should succeed'() {
        // given
        File src = IOUtils.mkdir(resource, 'src')
        File dest = IOUtils.mkdir(resource, 'dest')

        when(dependency.getRootDir()).thenReturn(src)
        IOUtils.write(src, 'main.go', 'This is main.go')
        // when
        installer.install(dependency, dest)
        // then
        assert dest.toPath().resolve('main.go').toFile().getText() == 'This is main.go'
    }
}
