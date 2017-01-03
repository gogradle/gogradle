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
    GitDependencyResolver resolver

    @Test
    void 'a GitNotationDependency should be resolved by GitDependencyResolver'() {
        // given
        when(injector.getInstance(GitDependencyResolver)).thenReturn(resolver)
        // when
        dependency.resolve()
        // then
        verify(resolver).resolve(dependency)
    }
}
