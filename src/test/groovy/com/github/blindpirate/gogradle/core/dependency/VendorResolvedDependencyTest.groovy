package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

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
    VendorResolvedDependency dependency
    @Mock
    VcsAccessor accessor

    ProjectCacheManager projectCacheManager = MockUtils.projectCacheManagerWithoutCache()

    File resource

    @Before
    void setUp() {
        when(hostDependency.getName()).thenReturn('host')
        when(hostDependency.getVersion()).thenReturn('version')
        when(hostDependency.formatVersion()).thenReturn('version')
        when(hostDependency.getVcsType()).thenReturn(VcsType.GIT)

        when(GogradleGlobal.INSTANCE.getInstance(ProjectCacheManager)).thenReturn(projectCacheManager)
        when(GogradleGlobal.INSTANCE.getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(dependencyVisitor.visitVendorDependencies(any(ResolvedDependency), any(File))).thenReturn(GolangDependencySet.empty())
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
        ReflectionUtils.setField(dependency, 'relativePathToHost', 'vendor/github.com/a/b')
        File dir = IOUtils.mkdir(resource, 'vendor/github.com/a/b/vendor/github.com/b/c')
        VendorResolvedDependency subDependency = VendorResolvedDependency.fromParent('github.com/b/c', dependency, dir)
        // then
        assert subDependency.hostDependency == hostDependency
        assert subDependency.relativePathToHost == 'vendor/github.com/a/b/vendor/github.com/b/c'
    }

    @Test
    void 'formatting a vendor dependency should succeed'() {
        assert dependency.formatVersion() == 'host#version/vendor/github.com/a/b'
    }

    @Test
    void 'update time of vendor resolved dependency in local directory should be the dir\'s last modified time'() {
        // given
        Project project = mock(Project)
        when(GogradleGlobal.INSTANCE.getInjector().getInstance(Project)).thenReturn(project)
        when(project.getRootDir()).thenReturn(resource)
        LocalDirectoryDependency hostDependency = LocalDirectoryDependency.fromLocal('local', resource)
        dependency = VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b'))
        // then
        assert dependency.updateTime == new File(resource, 'vendor/github.com/a/b').lastModified()
        assert dependency.isFirstLevel()
    }

    @Test(expected = IllegalStateException)
    void 'host dependency must be local or vcs'() {
        VendorResolvedDependency.fromParent('github.com/a/b', mock(ResolvedDependency), new File(resource, 'vendor/github.com/a/b'))
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

    @Test
    void 'equals and hashCode should be correct'() {
        assert dependency == dependency
        assert dependency != null
        assert dependency != mock(GolangDependency)
        assert dependency == VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b'))
        assert dependency.hashCode() == VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b')).hashCode()
    }

}
