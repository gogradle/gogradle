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
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.Key
import org.gradle.api.specs.Spec
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VcsResolvedDependencyTest {
    @Mock
    GitMercurialNotationDependency notationDependency
    @Mock
    Spec exclusionSpec

    @Before
    void setUp() {
        when(notationDependency.getTransitiveDepExclusions()).thenReturn([exclusionSpec] as Set)
        when(notationDependency.getName()).thenReturn('package')
    }

    VcsResolvedDependency newResolvedDependency(VcsType type) {
        return VcsResolvedDependency.builder(type)
                .withCommitId('commitId')
                .withCommitTime(42L)
                .withUrl('url')
                .withNotationDependency(notationDependency)
                .build()
    }

    @Test
    void 'updateTime should be read correctly'() {
        assert newResolvedDependency(VcsType.GIT).getUpdateTime() == 42L
        assert newResolvedDependency(VcsType.MERCURIAL).getUpdateTime() == 42L
    }

    @Test
    void 'a resolved dependency should be converted to notation successfully'() {
        // given
        VcsResolvedDependency dependency = newResolvedDependency(VcsType.GIT)
        // then
        assert dependency.toLockedNotation() == [name: 'package', commit: 'commitId', vcs: 'git', url: 'url']
    }

    @Test
    @WithMockInjector
    void 'getInstallerClass() should succeed'() {
        DependencyManager installer = mock(DependencyManager)
        when(GogradleGlobal.INSTANCE.getInjector().getInstance(Key.get(DependencyManager, Git))).thenReturn(installer)
        assert newResolvedDependency(VcsType.GIT).installer == installer
    }

    @Test
    void 'formatting should succeed'() {
        assert newResolvedDependency(VcsType.GIT).formatVersion() == 'commitI'
    }

    @Test
    void 'formatting with tag should succeed'() {
        // given
        VcsResolvedDependency dependency = newResolvedDependency(VcsType.GIT)
        ReflectionUtils.setField(dependency, 'tag', 'tag')

        // then
        assert dependency.formatVersion() == 'tag(commitI)'
    }

    @Test
    void 'dependencies should be equal if name/version/url/vcs equals'() {
        assert newResolvedDependency(VcsType.GIT) != newResolvedDependency(VcsType.MERCURIAL)

        def dependency1 = newResolvedDependency(VcsType.GIT)
        def dependency2 = newResolvedDependency(VcsType.GIT)

        assert dependency1 == dependency2
        assert dependency1 != null
        assert dependency1 == dependency1

        ReflectionUtils.setField(dependency1, 'tag', 'tag1')
        ReflectionUtils.setField(dependency2, 'tag', 'tag2')
        ReflectionUtils.setField(dependency1, 'updateTime', 0L)
        ReflectionUtils.setField(dependency2, 'updateTime', 1L)
        dependency1.firstLevel = true
        dependency2.firstLevel = false
        assert dependency1 == dependency2
        assert dependency1.hashCode() == dependency2.hashCode()
    }

    @Test
    void 'toString should succeed'() {
        assert newResolvedDependency(VcsType.GIT).toString() == 'package#commitId'
    }

}
