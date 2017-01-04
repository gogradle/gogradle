package com.github.blindpirate.gogradle.core.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.GogradleRootProduceStrategy
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Matchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class ResolveTaskTest {

    ResolveTask task
    @Mock
    GolangPluginSetting setting
    @Mock
    DependencyTreeFactory factory
    @Mock
    DependencyTreeNode tree
    @Mock
    ProjectInternal project
    @Mock
    File rootDir
    @Mock
    Path rootPath
    @Mock
    GogradleRootProduceStrategy strategy
    @Mock
    DependencyVisitor visitor

    @Before
    void setUp() {
        task = AbstractTask.injectIntoNewInstance(project, 'task', ResolveTask, { new ResolveTask() })
        ReflectionUtils.setField(task, 'setting', setting)
        ReflectionUtils.setField(task, 'dependencyTreeFactory', factory)
        ReflectionUtils.setField(task, 'strategy', strategy)
        ReflectionUtils.setField(task, 'visitor', visitor)
        when(rootDir.toPath()).thenReturn(rootPath)
        when(rootPath.toFile()).thenReturn(rootDir)
    }

    @Test
    void 'dependency resolution should success'() {
        // given
        when(setting.getPackagePath()).thenReturn("package")
        when(project.getRootDir()).thenReturn(rootDir)
        when(rootDir.lastModified()).thenReturn(1L)
        when(factory.getTree(any(AbstractResolvedDependency))).thenReturn(tree)

        // when
        task.resolve()

        // then
        assert task.dependencyTree.is(tree)
    }

}
