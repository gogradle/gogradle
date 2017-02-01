package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.VendorOnlyProduceStrategy
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithMockInjector
class VendorResolvedDependencyTest {

    @Mock
    AbstractResolvedDependency hostDependency
    @Mock
    DependencyInstaller hostDependencyInstaller
    @Mock
    DependencyVisitor dependencyVisitor
    @Mock
    VendorOnlyProduceStrategy vendorOnlyProduceStrategy
    @Mock
    File rootDir
    @Mock
    VendorResolvedDependency dependency

    @Before
    void setUp() {
        when(hostDependency.getName()).thenReturn('host')
        when(hostDependency.formatVersion()).thenReturn('version')
        when(GogradleGlobal.INSTANCE.getInstance(VendorOnlyProduceStrategy)).thenReturn(vendorOnlyProduceStrategy)
        when(GogradleGlobal.INSTANCE.getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(vendorOnlyProduceStrategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor))).thenReturn(GolangDependencySet.empty())
        when(GogradleGlobal.INSTANCE.getInstance(DependencyInstaller)).thenReturn(hostDependencyInstaller)
        when(hostDependency.getInstallerClass()).thenReturn(DependencyInstaller)

        dependency = VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, rootDir)
    }


    @Test
    void 'creating a vendor dependency should succeed'() {
        // then
        assert dependency.hostDependency == hostDependency
        assert dependency.dependencies.isEmpty()
        assert dependency.relativePathToHost.toString() == 'vendor/github.com/a/b'
    }

    @Test
    void 'creating a sub vendor dependency of vendor dependency should succeed'() {
        // when
        VendorResolvedDependency subDependency = VendorResolvedDependency.fromParent('github.com/b/c', dependency, mock(File))
        // then
        assert subDependency.hostDependency == hostDependency
        assert subDependency.relativePathToHost.toString() == 'vendor/github.com/a/b/vendor/github.com/b/c'
    }

    @Test
    void 'formatting a vendor dependency should succeed'() {
        // then
        assert dependency.formatVersion() == 'host#version/vendor/github.com/a/b'
    }

    @Test
    void 'installing a vendor dependency should succeed'() {
        // given
        File dest = mock(File)
        // when
        dependency.installTo(dest)
        // then
        verify(hostDependencyInstaller).install(dependency, dest)
    }

    @Test
    void 'notation should be generated correctly'() {
        // given
        when(hostDependency.toLockedNotation()).thenReturn([:])
        // then
        assert dependency.toLockedNotation() == [vendorPath: 'vendor/github.com/a/b']

    }

    @Test
    void 'installer class of vendor dependency hosting in LocalDirectoryDependency should be LocalDirectoryDependencyInstaller'() {
        ReflectionUtils.setField(dependency, 'hostDependency', mock(LocalDirectoryDependency))
        assert dependency.getInstallerClass() == LocalDirectoryDependencyInstaller
    }
}
