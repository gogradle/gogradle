package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

import java.time.Instant

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.Mockito.*
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
        DependencyVisitor visitor = mock(DependencyVisitor)
        GolangConfiguration configuration = mock(GolangConfiguration)
        when(visitor.visitExternalDependencies(any(ResolvedDependency), any(File), anyString())).thenReturn(new GolangDependencySet())
        when(visitor.visitSourceCodeDependencies(any(ResolvedDependency), any(File), anyString())).thenReturn(new GolangDependencySet())
        when(visitor.visitVendorDependencies(any(ResolvedDependency), any(File))).thenReturn(new GolangDependencySet())
        when(GogradleGlobal.INSTANCE.getInjector().getInstance(DependencyVisitor)).thenReturn(visitor)
        when(configuration.getName()).thenReturn(GolangConfiguration.TEST)
        // when
        def result = dependency.resolve(configuration)
        // then
        assert result.is(dependency)
        ArgumentCaptor captor = ArgumentCaptor.forClass(String)

        verify(visitor).visitExternalDependencies(any(ResolvedDependency), any(File), captor.capture())
        assert captor.value == 'test'

        verify(visitor).visitSourceCodeDependencies(any(ResolvedDependency), any(File), anyString())
        verify(visitor).visitVendorDependencies(any(ResolvedDependency), any(File))

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
    void 'transitive dependency exclusion should take effect'() {
        // given
        dependency.exclude(name: 'a')
        GolangDependency a = mockDependency('a')
        GolangDependency b = mockDependency('b')

        // when
        dependency.setDependencies(asGolangDependencySet(a, b))

        // then
        assert dependency.dependencies.size() == 1
        assert dependency.dependencies.first().name == 'b'
    }

    @Test
    void 'local dependency should be installed successfully'() {
        // given
        LocalDirectoryDependencyInstaller installer = mock(LocalDirectoryDependencyInstaller)
        File targetDirectory = mock(File)
        when(GogradleGlobal.INSTANCE.getInstance(LocalDirectoryDependencyInstaller)).thenReturn(installer)
        // when
        dependency.installTo(targetDirectory)
        // then
        verify(installer).install(dependency, targetDirectory)
    }

    @Test(expected = UnsupportedOperationException)
    void 'local dependency does not support getResolverClass()'() {
        dependency.getResolverClass()
    }

    void 'formatting should succeed'() {
        assert dependency.formatVersion() == resource.absolutePath
    }

}
