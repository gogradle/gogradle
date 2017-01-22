package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockResolvedDependency
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class LockTaskTest extends TaskTest {
    LockTask task

    ResolvedDependency build = mockResolvedDependency('build')
    ResolvedDependency test = mockResolvedDependency('test')

    @Mock
    DependencyTreeNode buildTree
    @Mock
    DependencyTreeNode testTree

    @Before
    void setUp() {
        task = buildTask(LockTask)
    }

    @Test
    void 'lock task should depend on resolve task'() {
        assertTaskDependsOn(task, RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'lock should succeed'() {
        // given
        GolangDependencySet buildSet = asGolangDependencySet(build)
        GolangDependencySet testSet = asGolangDependencySet(test)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(buildTree)
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getDependencyTree()).thenReturn(testTree)
        when(buildTree.flatten()).thenReturn(buildSet)
        when(buildTree.flatten()).thenReturn(testSet)
        // when
        task.lock()
        // then
        verify(lockedDependencyManager).lock([build] as Set, [test] as Set)
    }

}
