package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GitNotationDependencyTest extends MockInjectorSupport {

    GitNotationDependency dependency = new GitNotationDependency()

    @Mock
    GitDependencyManager gitDependencyManager

    @Test
    void 'a GitNotationDependency should be resolved by GitDependencyResolver'() {
        // given
        when(injector.getInstance(GitDependencyManager)).thenReturn(gitDependencyManager)
        // when
        dependency.resolve()
        // then
        verify(gitDependencyManager).resolve(dependency)
    }
}
