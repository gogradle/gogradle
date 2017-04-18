package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class InstallTaskTest extends TaskTest {

    InstallBuildDependenciesTask installBuildDependenciesTask
    InstallTestDependenciesTask installTestDependenciesTask
    GolangDependencySet golangDependencySet = GolangDependencySet.empty()

    @Mock
    DependencyTreeNode dependencyTree

    ResolvedDependency resolvedDependency = DependencyUtils.mockResolvedDependency('name')

    File resource

    @Before
    void setUp() {
        installBuildDependenciesTask = buildTask(InstallBuildDependenciesTask)
        installTestDependenciesTask = buildTask(InstallTestDependenciesTask)
        golangDependencySet.add(resolvedDependency)
    }

    @Test
    void 'getting serialization file should succeed'() {
        installBuildDependenciesTask.getSerializationFile()
        installTestDependenciesTask.getSerializationFile()
        verify(golangTaskContainer.get(ResolveBuildDependenciesTask)).getSerializationFile()
        verify(golangTaskContainer.get(ResolveTestDependenciesTask)).getSerializationFile()
    }

    @Test
    void 'getting installation directory should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getConfigurationName()).thenReturn('build')
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getConfigurationName()).thenReturn('test')
        Path buildGopath = mock(Path)
        Path testGopath = mock(Path)
        when(buildManager.getInstallationDirectory('build')).thenReturn(buildGopath)
        when(buildManager.getInstallationDirectory('test')).thenReturn(testGopath)

        // then
        installBuildDependenciesTask.getInstallationDirectory()
        verify(buildGopath).toFile()
        installTestDependenciesTask.getInstallationDirectory()
        verify(testGopath).toFile()
    }

    @Test
    void 'installing build dependencies should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(dependencyTree)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getConfigurationName()).thenReturn('build')
        when(dependencyTree.flatten()).thenReturn(golangDependencySet)
        when(buildManager.getInstallationDirectory('build')).thenReturn(resource.toPath())
        IOUtils.mkdir(resource, 'toBeRemoved')
        // when
        installBuildDependenciesTask.installDependencies()
        // then
        verify(buildManager).installDependency(resolvedDependency, 'build')
        assert !new File(resource, 'toBeRemoved').exists()
    }

    @Test
    void 'installing test dependencies should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getDependencyTree()).thenReturn(dependencyTree)
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getConfigurationName()).thenReturn('test')
        when(dependencyTree.flatten()).thenReturn(golangDependencySet)
        when(buildManager.getInstallationDirectory('test')).thenReturn(resource.toPath())
        IOUtils.mkdir(resource, 'toBeRemoved')
        // when
        installTestDependenciesTask.installDependencies()
        // then
        verify(buildManager).installDependency(resolvedDependency, 'test')
        assert !new File(resource, 'toBeRemoved').exists()
    }

}
