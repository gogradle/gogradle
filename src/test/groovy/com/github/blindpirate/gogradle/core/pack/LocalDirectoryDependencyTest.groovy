package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor

import java.time.Instant

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class LocalDirectoryDependencyTest {
    File resource

    LocalDirectoryDependency dependency

    @Before
    void setUp() {
        dependency = LocalDirectoryDependency.fromLocal('name', resource)
    }

    @Test
    void 'local directory should be resolved to itself'() {
        // given
        ResolveContext context = mock(ResolveContext)
        // when
        dependency.resolve(context)
        // then
        verify(context).produceTransitiveDependencies(dependency, resource)
    }

    @Test
    void 'version format of local directory should be its absolute path'() {
        assert dependency.formatVersion() == StringUtils.toUnixString(resource.toPath().toAbsolutePath())
    }

    @Test(expected = UnsupportedOperationException)
    void 'locking a local dependency should cause an exception'() {
        dependency.toLockedNotation()
    }

    @Test
    void 'version of a local dependency should be its timestamp'() {
        assert Instant.parse(dependency.getVersion()) > Instant.now().minusSeconds(60)
    }


    @Test(expected = DependencyResolutionException)
    void 'notation with invalid dir should cause an exception'() {
        LocalDirectoryDependency.fromLocal('', new File("inexistence"))
    }

    @Test
    void 'notation with valid dir should be resolved successfully'() {
        LocalDirectoryDependency.fromLocal('', resource)
    }


    @Test
    @WithResource('')
    void 'local dependency should be installed successfully'() {
        // given
        IOUtils.mkdir(resource, 'src')
        IOUtils.mkdir(resource, 'dest')
        File src = new File(resource, 'src')
        File dest = new File(resource, 'dest')

        dependency = LocalDirectoryDependency.fromLocal('name', src)
        LocalDirectoryDependencyInstaller installer = mock(LocalDirectoryDependencyInstaller)
        when(GogradleGlobal.INSTANCE.getInstance(LocalDirectoryDependencyInstaller)).thenReturn(installer)
        // when
        dependency.installTo(dest)
        // then
        verify(installer).install(dependency, dest)
        assert new File(dest, DependencyInstaller.CURRENT_VERSION_INDICATOR_FILE).text == StringUtils.toUnixString(src)
    }

    @Test(expected = UnsupportedOperationException)
    void 'local dependency does not support getResolverClass()'() {
        dependency.getResolverClass()
    }

    void 'formatting should succeed'() {
        assert dependency.formatVersion() == resource.absolutePath
    }

}
