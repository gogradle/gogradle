package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.build.BuildManager
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
    void 'installing build dependencies should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(dependencyTree)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getConfigurationName()).thenReturn('build')
        when(dependencyTree.flatten()).thenReturn(golangDependencySet)
        when(buildManager.getInstallationDirectory('build')).thenReturn(resource.toPath())
        IOUtils.mkdir(resource,'toBeRemoved')
        // when
        installBuildDependenciesTask.installDependencies()
        // then
        verify(buildManager).installDependency(resolvedDependency, 'build')
        assert !new File(resource,'toBeRemoved').exists()
    }

    @Test
    void 'installing test dependencies should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getDependencyTree()).thenReturn(dependencyTree)
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getConfigurationName()).thenReturn('test')
        when(dependencyTree.flatten()).thenReturn(golangDependencySet)
        when(buildManager.getInstallationDirectory('test')).thenReturn(resource.toPath())
        IOUtils.mkdir(resource,'toBeRemoved')
        // when
        installTestDependenciesTask.installDependencies()
        // then
        verify(buildManager).installDependency(resolvedDependency, 'test')
        assert !new File(resource,'toBeRemoved').exists()
    }

}
