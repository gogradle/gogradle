package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class InstallTaskTest extends TaskTest {

    InstallBuildDependenciesTask installBuildDependenciesTask
    InstallTestDependenciesTask installTestDependenciesTask
    GolangDependencySet golangDependencySet = GolangDependencySet.empty()

    @Mock
    DependencyTreeNode dependencyTree

    ResolvedDependency resolvedDependency = DependencyUtils.mockResolvedDependency('name')

    File resource

    @Before
    void setUp() {
        installBuildDependenciesTask = buildTask(InstallBuildDependenciesTask)
        installTestDependenciesTask = buildTask(InstallTestDependenciesTask)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getConfigurationName()).thenReturn('build')
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getConfigurationName()).thenReturn('test')
        golangDependencySet.add(resolvedDependency)
    }

    @Test
    void 'getting serialization file should succeed'() {
        installBuildDependenciesTask.getSerializationFile()
        installTestDependenciesTask.getSerializationFile()
        verify(golangTaskContainer.get(ResolveBuildDependenciesTask)).getSerializationFile()
        verify(golangTaskContainer.get(ResolveTestDependenciesTask)).getSerializationFile()
    }

    @Test
    void 'getting installation directory should succeed'() {
        // given
        Path buildGopath = mock(Path)
        Path testGopath = mock(Path)
        when(buildManager.getInstallationDirectory('build')).thenReturn(buildGopath)
        when(buildManager.getInstallationDirectory('test')).thenReturn(testGopath)

        // then
        installBuildDependenciesTask.getInstallationDirectory()
        verify(buildGopath).toFile()
        installTestDependenciesTask.getInstallationDirectory()
        verify(testGopath).toFile()
    }

    /*
    INSTALLATION_DIR
    \-- src
        |-- github.com
        |   \-- user
        |       |-- a VERSON exist, match
        |       |-- b VERSON exist, not match
        |       |-- c VERSON not exist, .go exist
        |       |-- d VESION not exist, .go not exist
        |       \-- e VESION exist
        |
        |-- f VERSION exist local directory
        \-- g VERSION exist vendor of local
     */

    @Test
    void 'installation should succeed'() {
        // given
        when(buildManager.getInstallationDirectory('build')).thenReturn(resource.toPath())
        IOUtils.write(resource, 'src/github.com/user/a/.CURRENT_VERSION', 'a')
        IOUtils.write(resource, 'src/github.com/user/b/.CURRENT_VERSION', 'unmatchedB')
        IOUtils.write(resource, 'src/github.com/user/c/main.go', '')
        IOUtils.write(resource, 'src/github.com/user/d/main.c', '')
        IOUtils.write(resource, 'src/github.com/user/e/.CURRENT_VERSION', 'e')
        IOUtils.write(resource, 'src/f/.CURRENT_VERSION', 'whatever')
        IOUtils.write(resource, 'src/g/.CURRENT_VERSION', 'whatever')
        ResolvedDependency a = DependencyUtils.mockResolvedDependency('github.com/user/a')
        ResolvedDependency b = DependencyUtils.mockResolvedDependency('github.com/user/b')
        ResolvedDependency c = DependencyUtils.mockResolvedDependency('github.com/user/c')
        ResolvedDependency d = DependencyUtils.mockResolvedDependency('github.com/user/d')
        when(a.getVersion()).thenReturn('a')
        when(b.getVersion()).thenReturn('b')
        when(c.getVersion()).thenReturn('c')
        when(d.getVersion()).thenReturn('d')

        LocalDirectoryDependency f = DependencyUtils.mockWithName(LocalDirectoryDependency, 'f')
        VendorResolvedDependency g = DependencyUtils.mockWithName(VendorResolvedDependency, 'g')
        when(f.getVersion()).thenReturn('f')
        when(g.getVersion()).thenReturn('g')
        when(g.getHostDependency()).thenReturn(f)

        GolangDependencySet dependencies = DependencyUtils.asGolangDependencySet(a, b, c, d, f, g)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(dependencyTree)
        when(dependencyTree.flatten()).thenReturn(dependencies)

        // when
        installBuildDependenciesTask.installDependencies()

        // then
        verify(a, times(0)).installTo(new File(resource, 'src/github.com/user/a'))
        verify(b).installTo(new File(resource, 'src/github.com/user/b'))
        assert IOUtils.toString(new File(resource, 'src/github.com/user/b/.CURRENT_VERSION')) == 'b'
        verify(c).installTo(new File(resource, 'src/github.com/user/c'))
        assert IOUtils.toString(new File(resource, 'src/github.com/user/c/.CURRENT_VERSION')) == 'c'
        verify(d).installTo(new File(resource, 'src/github.com/user/d'))
        assert IOUtils.toString(new File(resource, 'src/github.com/user/d/.CURRENT_VERSION')) == 'd'

        assert IOUtils.dirIsEmpty(new File(resource, 'src/github.com/user/e'))

        verify(f).installTo(new File(resource, 'src/f'))
        assert IOUtils.toString(new File(resource, 'src/f/.CURRENT_VERSION')) == 'f'
        verify(g).installTo(new File(resource, 'src/g'))
        assert IOUtils.toString(new File(resource, 'src/g/.CURRENT_VERSION')) == 'g'
    }
}
