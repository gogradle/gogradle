package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.VendorOnlyProduceStrategy
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Paths

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithMockInjector
@WithResource('')
class VendorResolvedDependencyTest {

    @Mock
    VcsResolvedDependency hostDependency
    @Mock
    DependencyInstaller hostDependencyInstaller
    @Mock
    DependencyVisitor dependencyVisitor
    @Mock
    VendorOnlyProduceStrategy vendorOnlyProduceStrategy
    @Mock
    VendorResolvedDependency dependency
    @Mock
    VcsAccessor accessor

    File resource

    @Before
    void setUp() {
        when(hostDependency.getName()).thenReturn('host')
        when(hostDependency.getVersion()).thenReturn('version')
        when(hostDependency.formatVersion()).thenReturn('version')
        when(hostDependency.getVcsType()).thenReturn(VcsType.GIT)

        when(GogradleGlobal.INSTANCE.getInstance(VendorOnlyProduceStrategy)).thenReturn(vendorOnlyProduceStrategy)
        when(GogradleGlobal.INSTANCE.getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(vendorOnlyProduceStrategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor), anyString())).thenReturn(GolangDependencySet.empty())
        when(GogradleGlobal.INSTANCE.getInstance(Key.get(VcsAccessor, Git))).thenReturn(accessor)
        when(hostDependency.getInstaller()).thenReturn(hostDependencyInstaller)

        IOUtils.mkdir(resource, 'vendor/github.com/a/b')
        dependency = VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b'))
    }


    @Test
    void 'creating a vendor dependency should succeed'() {
        // then
        assert dependency.hostDependency == hostDependency
        assert dependency.dependencies.isEmpty()
        assert toUnixString(dependency.relativePathToHost) == 'vendor/github.com/a/b'
    }

    @Test
    void 'creating a sub vendor dependency of vendor dependency should succeed'() {
        // when
        ReflectionUtils.setField(dependency, 'relativePathToHost', Paths.get('vendor/github.com/a/b'))
        File dir = IOUtils.mkdir(resource, 'vendor/github.com/a/b/vendor/github.com/b/c')
        VendorResolvedDependency subDependency = VendorResolvedDependency.fromParent('github.com/b/c', dependency, dir)
        // then
        assert subDependency.hostDependency == hostDependency
        assert toUnixString(subDependency.relativePathToHost) == 'vendor/github.com/a/b/vendor/github.com/b/c'
    }

    @Test
    void 'formatting a vendor dependency should succeed'() {
        assert dependency.formatVersion() == 'host#version/vendor/github.com/a/b'
    }

    @Test
    void 'installing a vendor dependency should succeed'() {
        assert dependency.installer.is(hostDependencyInstaller)
    }

    @Test
    void 'notation should be generated correctly'() {
        // given
        when(hostDependency.toLockedNotation()).thenReturn([:])
        // then
        assert dependency.toLockedNotation() == [name: 'github.com/a/b', vendorPath: 'vendor/github.com/a/b', host: [:]]

    }

    @Test
    void 'installer class of vendor dependency hosting in LocalDirectoryDependency should be LocalDirectoryDependencyInstaller'() {
        LocalDirectoryDependencyInstaller installer = mock(LocalDirectoryDependencyInstaller)
        when(GogradleGlobal.getInstance(LocalDirectoryDependencyInstaller)).thenReturn(installer)
        ReflectionUtils.setField(dependency, 'hostDependency', mock(LocalDirectoryDependency))
        assert dependency.getInstaller().is(installer)
    }
}
