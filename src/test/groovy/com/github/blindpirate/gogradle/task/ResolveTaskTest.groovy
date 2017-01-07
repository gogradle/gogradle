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
    ResolveTask task
    File resource
    @Mock
    DependencyTreeNode tree
    @Mock
    Path rootPath

    @Before
    void setUp() {
        task = buildTask(ResolveTask)
        when(rootPath.toFile()).thenReturn(resource)
    }

    @Test
    void 'dependency resolution should succeed'() {
        // given
        when(setting.getPackagePath()).thenReturn("package")
        when(project.getRootDir()).thenReturn(resource)
        when(dependencyTreeFactory.getTree(any(LocalDirectoryDependency))).thenReturn(tree)

        // when
        task.resolve()

        // then
        assert task.dependencyTree.is(tree)
    }


}
