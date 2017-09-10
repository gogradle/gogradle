/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.CacheScope
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
    void 'getting cache scope should succeed'() {
        assert dependency.cacheScope == CacheScope.PERSISTENCE

        dependency.commit = ''
        assert dependency.cacheScope == CacheScope.BUILD

        dependency.commit = 'LATEST_COMMIT'
        assert dependency.cacheScope == CacheScope.BUILD
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
        GitMercurialNotationDependency dependency1 = withNameAndCommit('name', 'LATEST_COMMIT')
        GitMercurialNotationDependency dependency2 = withNameAndCommit('name', 'LATEST_COMMIT')
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
        GitMercurialNotationDependency dependency1 = withNameAndCommit('name', 'LATEST_COMMIT')
        GitMercurialNotationDependency dependency2 = withNameAndCommit('name', '12345')
        // then
        assert dependency1 != dependency2
    }

    @Test
    void 'toString should succeed'() {
        dependency.url = 'https://github.com/a/b.git'
        assert dependency.toString() == "github.com/a/b: commit='commitId', urls=[https://github.com/a/b.git]"
        dependency.tag = '1.0.0'
        assert dependency.toString() == "github.com/a/b: commit='commitId', tag/branch='1.0.0', urls=[https://github.com/a/b.git]"
        dependency.commit = null
        assert dependency.toString() == "github.com/a/b: tag/branch='1.0.0', urls=[https://github.com/a/b.git]"
    }

    @Test
    void 'equals and hashCode should succeed'() {
        assert dependency.equals(dependency)
        assert !dependency.equals(null)
        assert dependency != new LocalDirectoryDependency()

        GitMercurialNotationDependency dependency2 = new GitNotationDependency()
        dependency2.name = 'github.com/a/b'
        dependency2.commit = 'commitId'
        dependency2.url = 'url'
        assert dependency == dependency2
        assert dependency.hashCode() == dependency2.hashCode()

        dependency2.url = 'anotherurl'
        assert dependency != dependency2
        assert dependency.hashCode() != dependency2.hashCode()

        dependency2.url = 'url'
        dependency2.name = 'github.com/a/c'
        assert dependency != dependency2
        assert dependency.hashCode() != dependency2.hashCode()

        dependency2.name = 'github.com/a/b'
        dependency2.commit = 'anotherCommit'

        assert dependency != dependency2
        assert dependency.hashCode() != dependency2.hashCode()
    }

    GitMercurialNotationDependency withNameAndCommit(String name, String commit) {
        GitMercurialNotationDependency ret = new GitNotationDependency()
        ret.setName(name)
        ret.setCommit(commit)
        return ret
    }
}
