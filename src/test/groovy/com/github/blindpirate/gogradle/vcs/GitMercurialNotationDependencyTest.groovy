package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithMockInjector
class GitMercurialNotationDependencyTest {

    GitMercurialNotationDependency dependency = new GitNotationDependency()

    @Mock
    GitDependencyManager gitDependencyManager

    @Mock
    ResolveContext context

    @Before
    void setUp() {
        dependency.name = 'github.com/a/b'
        dependency.commit = 'commitId'
        dependency.url = 'url'
    }

    @Test
    void 'a GitNotationDependency should be resolved by GitDependencyResolver'() {
        // given
        when(GogradleGlobal.INSTANCE.getInstance(GitDependencyManager)).thenReturn(gitDependencyManager)
        // when
        dependency.resolve(context)
        // then
        verify(gitDependencyManager).resolve(context, dependency)
    }

    @Test
    void 'git dependencies with same name and commit but not url should not be equal'() {
        // when
        GitMercurialNotationDependency dependency1 = withNameAndCommit('name', 'NEWEST_COMMIT')
        GitMercurialNotationDependency dependency2 = withNameAndCommit('name', 'NEWEST_COMMIT')
        // then
        assert dependency1 == dependency2

        // when
        dependency1.setUrl('git@github.com:a/b.git')
        dependency2.setUrl('https://github.com/a/b.git')
        assert dependency1 != dependency2
    }

    @Test
    void 'git dependencies with different name or commit should not be equal'() {
        // when
        GitMercurialNotationDependency dependency1 = withNameAndCommit('name', 'NEWEST_COMMIT')
        GitMercurialNotationDependency dependency2 = withNameAndCommit('name', '12345')
        // then
        assert dependency1 != dependency2
    }

    @Test
    void 'toString should succeed'() {
        dependency.url = 'https://github.com/a/b.git'
        assert dependency.toString() == "GitNotationDependency{name='github.com/a/b', commit='commitId', urls='[https://github.com/a/b.git]'}"
        dependency.tag = '1.0.0'
        assert dependency.toString() == "GitNotationDependency{name='github.com/a/b', commit='commitId', tag='1.0.0', urls='[https://github.com/a/b.git]'}"
        dependency.commit = null
        assert dependency.toString() == "GitNotationDependency{name='github.com/a/b', tag='1.0.0', urls='[https://github.com/a/b.git]'}"
    }

    @Test
    void 'equals should succeed'() {
        assert dependency.equals(dependency)
        assert !dependency.equals(null)
        assert dependency != new LocalDirectoryDependency()

        GitMercurialNotationDependency dependency2 = new GitNotationDependency()
        dependency2.name = 'github.com/a/b'
        dependency2.commit = 'commitId'
        dependency2.url = 'url'
        assert dependency == dependency2

        dependency2.url = 'anotherurl'
        assert dependency != dependency2

        dependency2.url = 'url'
        dependency2.name = 'github.com/a/c'
        assert dependency != dependency2

        dependency2.name = 'github.com/a/b'
        dependency2.commit = 'anotherCommit'

        assert dependency != dependency2
    }

    @Test
    void 'hashCode should succeed'() {
        assert dependency.hashCode() == Objects.hash('commitId', ['url'], Objects.hash([] as Set, 'github.com/a/b', false))
    }

    GitMercurialNotationDependency withNameAndCommit(String name, String commit) {
        GitMercurialNotationDependency ret = new GitNotationDependency()
        ret.setName(name)
        ret.setCommit(commit)
        return ret
    }
}
