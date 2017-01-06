package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.vcs.git.GitDependencyResolver
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class DefaultDependencyInstallerTest extends MockInjectorSupport {
    DefaultDependencyInstaller installer
    @Mock
    GitDependencyResolver resolver
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    Project project

    File resource

    @Before
    void setUp() {
        installer = new DefaultDependencyInstaller(project)
        when(resolvedDependency.getName()).thenReturn('root/package')
        when(project.getRootDir()).thenReturn(resource)
        when(resolvedDependency.getResolverClass()).thenReturn(GitDependencyResolver)
        when(injector.getInstance(GitDependencyResolver)).thenReturn(resolver)
    }

    @Test
    void 'copying a dependency from global cache to project cache should succeed'() {
        // when
        installer.installDependency(resolvedDependency)
        // then
        Path gopath = resource.toPath().resolve('.gogradle/build_gopath/root/package')
        assert gopath.toFile().exists()
        verify(resolver).reset(resolvedDependency, gopath.toFile())
    }
}
