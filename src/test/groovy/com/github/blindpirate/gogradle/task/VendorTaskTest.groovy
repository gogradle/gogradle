package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class VendorTaskTest extends TaskTest {

    VendorTask task

    ResolvedDependency resolvedDependency = DependencyUtils.mockResolvedDependency('a')

    File resource

    @Mock
    DependencyTreeNode buildTree

    @Before
    void setUp() {
        task = buildTask(VendorTask)
        when(project.getRootDir()).thenReturn(resource)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(buildTree)

        GolangDependencySet set = DependencyUtils.asGolangDependencySet(resolvedDependency)
        when(buildTree.flatten()).thenReturn(set)
    }

    @Test
    void 'vendor task should depend on install task'() {
        assertTaskDependsOn(task, GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'vendor directory should be cleared before installing'() {
        // given
        IOUtils.mkdir(resource, 'vendor/a')
        IOUtils.mkdir(resource, 'vendor/b')
        IOUtils.write(resource, 'vendor/vendor.json', '')
        // when
        task.vendor()
        // then
        assert !new File(resource, 'vendor/a').exists()
        assert !new File(resource, 'vendor/b').exists()
        assert new File(resource, 'vendor/vendor.json').exists()
    }

    @Test
    void 'vendor should be installed successfully'() {
        // when
        task.vendor()
        // then
        verify(buildManager).installDependencyToVendor(resolvedDependency)
    }
}
