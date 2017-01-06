package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockResolvedDependency
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class InstallDependenciesTaskTest extends TaskTest {
    InstallDependenciesTask task
    @Mock
    DependencyTreeNode rootNode


    @Before
    void setUp() {
        task = buildTask(InstallDependenciesTask)
    }

    @Test
    void 'installing dependencies should succeed'() {
        // given
        when(golangTaskContainer.get(ResolveTask).getDependencyTree()).thenReturn(rootNode)
        ResolvedDependency resolvedDependency = mockResolvedDependency('notationDependency')
        GolangDependencySet dependencies = asGolangDependencySet(resolvedDependency)
        when(rootNode.flatten()).thenReturn(dependencies)
        // when
        task.installDependencies()
        // then
        verify(dependencyInstaller).installDependency(resolvedDependency)
    }

}
