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
import com.github.blindpirate.gogradle.core.GolangRepository
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.MockUtils
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
class VcsNotationDependencyTest {

    VcsNotationDependency dependency = new GitNotationDependency()

    @Mock
    GitDependencyManager gitDependencyManager

    @Mock
    ResolveContext context

    @Before
    void setUp() {
        dependency.name = 'github.com/user/package'
        dependency.commit = 'commitId'
        dependency.package = MockUtils.mockRootVcsPackage()
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
        VcsNotationDependency dependency1 = withNameAndCommit('name', 'LATEST_COMMIT')
        VcsNotationDependency dependency2 = withNameAndCommit('name', 'LATEST_COMMIT')
        // then
        assert dependency1 == dependency2

        // when
        dependency1.setPackage(withUrl('git@github.com:user/package.git'))
        dependency2.setPackage(withUrl('https://github.com/user/package.git'))
        assert dependency1 != dependency2
    }

    VcsGolangPackage withUrl(String url) {
        return VcsGolangPackage.builder()
                .withRootPath('github.com/user/package')
                .withPath('github.com/user/package')
                .withRepository(GolangRepository.newOriginalRepository(VcsType.GIT, url))
                .build()
    }

    @Test
    void 'git dependencies with different name or commit should not be equal'() {
        // when
        VcsNotationDependency dependency1 = withNameAndCommit('name', 'LATEST_COMMIT')
        VcsNotationDependency dependency2 = withNameAndCommit('name', '12345')
        // then
        assert dependency1 != dependency2
    }

    @Test
    void 'toString should succeed'() {
        dependency.setPackage(withUrl('https://github.com/user/package.git'))
        assert dependency.toString() == "github.com/user/package: commit='commitId', urls=[https://github.com/user/package.git]"
        dependency.tag = '1.0.0'
        assert dependency.toString() == "github.com/user/package: commit='commitId', tag='1.0.0', urls=[https://github.com/user/package.git]"
        dependency.commit = null
        assert dependency.toString() == "github.com/user/package: tag='1.0.0', urls=[https://github.com/user/package.git]"
        dependency.tag = null
        dependency.branch = 'master'
        assert dependency.toString() == "github.com/user/package: branch='master', urls=[https://github.com/user/package.git]"
    }

    @Test
    void 'equals and hashCode should succeed'() {
        assert dependency.equals(dependency)
        assert !dependency.equals(null)
        assert dependency != new LocalDirectoryDependency()

        VcsNotationDependency dependency2 = new GitNotationDependency()
        dependency2.name = 'github.com/user/package'
        dependency2.commit = 'commitId'
        dependency2.setPackage(MockUtils.mockRootVcsPackage())
        assert dependency == dependency2
        assert dependency.hashCode() == dependency2.hashCode()

        dependency2.setPackage(withUrl('anotherurl'))
        assert dependency != dependency2
        assert dependency.hashCode() != dependency2.hashCode()

        dependency2.setPackage(withUrl('url'))
        dependency2.name = 'github.com/a/c'
        assert dependency != dependency2
        assert dependency.hashCode() != dependency2.hashCode()

        dependency2.name = 'github.com/a/b'
        dependency2.commit = 'anotherCommit'

        assert dependency != dependency2
        assert dependency.hashCode() != dependency2.hashCode()
    }

    VcsNotationDependency withNameAndCommit(String name, String commit) {
        VcsNotationDependency ret = new GitNotationDependency()
        ret.setName(name)
        ret.setCommit(commit)
        ret.setPackage(MockUtils.mockRootVcsPackage())
        return ret
    }
}
