package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DependenciesTaskTest extends TaskTest {
    DependenciesTask task

    @Mock
    DependencyTreeNode root

    @Before
    void setUp() {
        task = buildTask(DependenciesTask)
        when(golangTaskContainer.get(ResolveTask).getDependencyTree()).thenReturn(root)
        when(root.output()).thenReturn('output')
    }


    @Test
    void 'dependency tree should be displayed correctly'() {
        task.displayDependencies()
    }

}
