package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DependenciesTaskTest extends TaskTest {
    DependenciesTask task

    @Mock
    DependencyTreeNode buildTree
    @Mock
    DependencyTreeNode testTree
    @Mock
    Logger logger

    @Before
    void setUp() {
        task = buildTask(DependenciesTask)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(buildTree)
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getDependencyTree()).thenReturn(testTree)
        when(buildTree.output()).thenReturn('build output')
        when(testTree.output()).thenReturn('test output')
        ReflectionUtils.setStaticFinalField(DependenciesTask, 'LOGGER', logger)
    }

    @After
    void cleanUp() {
        ReflectionUtils.setStaticFinalField(DependenciesTask, 'LOGGER', Logging.getLogger(DependenciesTask))
    }

    @Test
    void 'dependencies task should depends on resolve task'(){
        assertTaskDependsOn(task,RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task,RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'dependency tree should be displayed correctly'() {
        // when
        task.displayDependencies()
        // then
        verify(logger).quiet('build:')
        verify(logger).quiet('test:')
        verify(logger).quiet('build output')
        verify(logger).quiet('test output')
    }

}
