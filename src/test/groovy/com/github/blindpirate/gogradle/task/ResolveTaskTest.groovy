package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Matchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class ResolveTaskTest extends TaskTest {
    ResolveBuildDependenciesTask resolveBuildDependenciesTask
    ResolveTestDependenciesTask resolveTestDependenciesTask

    File resource
    @Mock
    DependencyTreeNode tree
    @Mock
    Path rootPath

    @Before
    void setUp() {
        resolveBuildDependenciesTask = buildTask(ResolveBuildDependenciesTask)
        resolveTestDependenciesTask = buildTask(ResolveTestDependenciesTask)
        when(rootPath.toFile()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn("package")
        when(project.getRootDir()).thenReturn(resource)
        when(dependencyTreeFactory.getTree(any(LocalDirectoryDependency))).thenReturn(tree)
    }

    @Test
    void 'build dependency resolution should succeed'() {
        // when
        resolveBuildDependenciesTask.resolve()
        // then
        assert resolveBuildDependenciesTask.dependencyTree.is(tree)
    }
    @Test
    void 'test dependency resolution should succeed'() {
        // when
        resolveTestDependenciesTask.resolve()
        // then
        assert resolveTestDependenciesTask.dependencyTree.is(tree)
    }


}
