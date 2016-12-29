package com.github.blindpirate.gogradle.core.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyTreeNode
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyTreeFactory
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Matchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class ResolveTaskTest {

    private ResolveTask task
    @Mock
    private GolangPluginSetting setting
    @Mock
    private DependencyTreeFactory factory
    @Mock
    private DependencyTreeNode tree
    @Mock
    private ProjectInternal project
    @Mock
    private File rootDir
    @Mock
    private Path rootPath

    @Before
    void setUp() {
        task = AbstractTask.injectIntoNewInstance(project, 'task', ResolveTask, { new ResolveTask() })
        ReflectionUtils.setField(task, 'setting', setting)
        ReflectionUtils.setField(task, 'dependencyTreeFactory', factory)
        when(rootDir.toPath()).thenReturn(rootPath)
        when(rootPath.toFile()).thenReturn(rootDir)
    }

    @Test
    void 'dependency resolution should success'() {
        // given
        when(setting.getPackagePath()).thenReturn("package")
        when(project.getRootDir()).thenReturn(rootDir)
        when(rootDir.lastModified()).thenReturn(1L)
        when(factory.getTree(any(GolangPackageModule))).thenReturn(tree)

        // when
        task.resolve()

        // then
        assert task.dependencyTree.is(tree)
    }

}
