package com.github.blindpirate.gogradle.core.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Matchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class ResolveTaskTest extends TaskTest {
    ResolveTask task
    @Mock
    DependencyTreeNode tree
    @Mock
    File rootDir
    @Mock
    Path rootPath

    @Before
    void setUp() {
        task = buildTask(ResolveTask)
        when(rootDir.toPath()).thenReturn(rootPath)
        when(rootPath.toFile()).thenReturn(rootDir)
    }

    @Test
    void 'dependency resolution should success'() {
        // given
        when(setting.getPackagePath()).thenReturn("package")
        when(project.getRootDir()).thenReturn(rootDir)
        when(rootDir.lastModified()).thenReturn(1L)
        when(dependencyTreeFactory.getTree(any(AbstractResolvedDependency))).thenReturn(tree)

        // when
        task.resolve()

        // then
        assert task.dependencyTree.is(tree)
    }


}
