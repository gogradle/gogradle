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

    @Test
    void 'git dependencies with same name and commit should be equal'() {
        // when
        GitNotationDependency dependency1 = withNameAndCommit('name', 'NEWEST_COMMIT')
        GitNotationDependency dependency2 = withNameAndCommit('name', 'NEWEST_COMMIT')
        // then
        assert dependency1 == dependency2

        // when
        dependency1.setUrl('git@github.com:a/b.git')
        dependency2.setUrl('https://github.com/a/b.git')
        assert dependency1 == dependency2
    }

    @Test
    void 'git dependencies with different name or commit should not be equal'() {
        // when
        GitNotationDependency dependency1 = withNameAndCommit('name', 'NEWEST_COMMIT')
        GitNotationDependency dependency2 = withNameAndCommit('name', '12345')
        // then
        assert dependency1 != dependency2
    }

    GitNotationDependency withNameAndCommit(String name, String commit) {
        GitNotationDependency ret = new GitNotationDependency()
        ret.setName(name)
        ret.setCommit(commit)
        return ret
    }
}
