package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockResolvedDependency
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Matchers.any
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class ResolveTaskTest extends TaskTest {
    ResolveBuildDependenciesTask resolveBuildDependenciesTask
    ResolveTestDependenciesTask resolveTestDependenciesTask

    File resource
    @Mock
    DependencyTreeNode tree
    @Mock
    Path rootPath
    @Mock
    GolangConfiguration configuration
    @Mock
    DependencyVisitor dependencyVisitor

    ResolvedDependency resolvedDependency = mockResolvedDependency('notationDependency')

    @Before
    void setUp() {
        resolveBuildDependenciesTask = buildTask(ResolveBuildDependenciesTask)
        resolveTestDependenciesTask = buildTask(ResolveTestDependenciesTask)

        when(configurationManager.getByName(anyString())).thenReturn(configuration)
        when(strategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor), anyString())).thenReturn(GolangDependencySet.empty())

        when(buildManager.getInstallationDirectory('build')).thenReturn(new File(resource, '.gogradle/build_gopath').toPath())
        when(buildManager.getInstallationDirectory('test')).thenReturn(new File(resource, '.gogradle/test_gopath').toPath())

        when(rootPath.toFile()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn("package")
        when(project.getRootDir()).thenReturn(resource)
        when(GogradleGlobal.INSTANCE.getInjector().getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(dependencyTreeFactory.getTree(any(ResolveContext), any(LocalDirectoryDependency))).thenReturn(tree)
        GolangDependencySet dependencies = asGolangDependencySet(resolvedDependency)
        when(tree.flatten()).thenReturn(dependencies)
    }

    @Test
    void 'build dependency resolution should succeed'() {
        // when
        when(configuration.getName()).thenReturn('build')
        resolveBuildDependenciesTask.resolve()
        // then
        verify(buildManager).installDependency(resolvedDependency, 'build')
        assert resolveBuildDependenciesTask.dependencyTree.is(tree)
    }

    @Test
    void 'test dependency resolution should succeed'() {
        // when
        when(configuration.getName()).thenReturn('test')
        resolveTestDependenciesTask.resolve()
        // then
        verify(buildManager).installDependency(resolvedDependency, 'test')
        assert resolveTestDependenciesTask.dependencyTree.is(tree)
    }

    @Test
    void 'checking for external files should succeed'() {
        when(GogradleGlobal.INSTANCE.getInjector().getInstance((Class) any(Class))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0).newInstance()
            }
        })

        ['gogradle.lock', 'glide.lock', 'vendor/vendor.json'].each {
            IOUtils.write(resource, it, '')
        }

        List<File> externalLockFiles = resolveBuildDependenciesTask.getExternalLockfiles()

        assert externalLockFiles.size() == 3
        assert externalLockFiles.any { it.name == 'gogradle.lock' }
        assert externalLockFiles.any { it.name == 'glide.lock' }
        assert externalLockFiles.any { it.name == 'vendor.json' }
    }

    @Test
    void 'resolve task should depend on preprare task'() {
        assertTaskDependsOn(resolveBuildDependenciesTask, PREPARE_TASK_NAME)
        assertTaskDependsOn(resolveTestDependenciesTask, PREPARE_TASK_NAME)
    }

}
