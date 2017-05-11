package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependencyForTest
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class ResolveTestDependenciesTaskTest extends TaskTest {
    ResolveTestDependenciesTask task

    File resource

    @Mock
    DependencyTreeNode buildDependencyTree

    @Before
    void setUp() {
        task = buildTask(ResolveTestDependenciesTask)
        when(project.getRootDir()).thenReturn(resource)
    }

    @Test
    void 'test dependencies should remove all that has existed in build dependencies'() {
        // given
        VendorResolvedDependency vendorA = new VendorResolvedDependencyForTest('a',
                'versionA',
                1L,
                gogradleRootProject,
                'vendor/a')
        VendorResolvedDependency vendorB = new VendorResolvedDependencyForTest('b',
                'versionB',
                2L,
                gogradleRootProject,
                'vendor/b')
        VendorResolvedDependency vendorC = new VendorResolvedDependencyForTest('c',
                'versionC',
                3L,
                gogradleRootProject,
                'vendor/a/vendor/c')

        vendorA.dependencies.add(vendorC)
        GolangDependencySet buildDependencies = asGolangDependencySet(vendorA, vendorB, vendorC)

        GolangDependencySet testDependencies = asGolangDependencySet(mockDependency('c'), mockDependency('d'))


        when(getGolangTaskContainer().get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(buildDependencyTree)
        when(buildDependencyTree.flatten()).thenReturn(buildDependencies)
        when(strategy.produce(gogradleRootProject, resource, visitor, 'test')).thenReturn(testDependencies)
        // when
        task.resolve()
        // then
        ArgumentCaptor captor = ArgumentCaptor.forClass(ResolvedDependency)
        verify(gogradleRootProject).setDependencies(captor.capture())
        assert captor.value.size() == 1
        assert captor.value.first().name == 'd'
    }
}
