package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.build.Configuration.BUILD
import static com.github.blindpirate.gogradle.build.Configuration.TEST
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockResolvedDependency
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class InstallDependenciesTaskTest extends TaskTest {
    InstallBuildDependenciesTask installBuildDependenciesTask
    InstallTestDependenciesTask installTestDependenciesTask

    @Mock
    DependencyTreeNode rootNode

    ResolvedDependency resolvedDependency = mockResolvedDependency('notationDependency')

    @Before
    void setUp() {
        installBuildDependenciesTask = buildTask(InstallBuildDependenciesTask)
        installTestDependenciesTask = buildTask(InstallTestDependenciesTask)
        GolangDependencySet dependencies = asGolangDependencySet(resolvedDependency)
        when(rootNode.flatten()).thenReturn(dependencies)
    }

    @Test
    void 'install task should depend on resolve task'() {
        assertTaskDependsOn(installBuildDependenciesTask, RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(installTestDependenciesTask, RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'installing build dependencies should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(rootNode)
        // when
        installBuildDependenciesTask.installDependencies()
        // then
        verify(buildManager).installDependency(resolvedDependency, BUILD)
    }

    @Test
    void 'installing test dependencies should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getDependencyTree()).thenReturn(rootNode)
        // when
        installTestDependenciesTask.installDependencies()
        // then
        verify(buildManager).installDependency(resolvedDependency, TEST)
    }


}
