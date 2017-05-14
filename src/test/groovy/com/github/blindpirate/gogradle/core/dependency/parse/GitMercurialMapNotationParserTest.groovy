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

package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.DependencyUtils.getExclusionSpecs

@RunWith(GogradleRunner)
class GitMercurialMapNotationParserTest {
    VcsGolangPackage pkg = VcsGolangPackage.builder()
            .withPath('github.com/a/b')
            .withRootPath('github.com/a/b')
            .withOriginalVcsInfo(VcsType.GIT, ['url'])
            .build()

    GitMercurialMapNotationParser parser = new GitMercurialMapNotationParser()

    void assertWithNameAndUrl(GitMercurialNotationDependency dependency) {
        assert dependency instanceof GitNotationDependency
        assert dependency.name == 'github.com/a/b'
        assert dependency.urls == ['url']
    }

    void assertEmpty(GitMercurialNotationDependency dependency, String... properties) {
        properties.each {
            assert !ReflectionUtils.getField(dependency, it)
        }
    }

    @Test
    void 'map notation with tag should be parsed correctly'() {
        // when
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', tag: 'v1.0.0', package: pkg])
        // then
        assertWithNameAndUrl(dependency)
        assertEmpty(dependency, 'commit')
        assert dependency.tag == 'v1.0.0'
    }

    @Test
    void 'mercurial map notation should be parsed correctly'() {
        // when
        pkg = VcsGolangPackage.builder()
                .withRootPath('bitbucket.org/a/b')
                .withPath('bitbucket.org/a/b')
                .withOriginalVcsInfo(VcsType.MERCURIAL, ['url'])
                .build()
        GitMercurialNotationDependency dependency = parser.parse([name: 'bitbucket.org/a/b', version: 'v1.0.0', vcs: 'hg', package: pkg])
        // then
        assert dependency instanceof GitMercurialNotationDependency
        assert dependency.name == 'bitbucket.org/a/b'
        assert !dependency.tag
        assert dependency.urls == ['url']
        assert dependency.commit == 'v1.0.0'
    }

    @Test
    void 'url in map notation should not be overwritten'() {
        // when
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', url: 'specifiedUrl', package: pkg])
        // then
        assert dependency.name == 'github.com/a/b'
        assertEmpty(dependency, 'tag',)
        assert dependency.urls == ['specifiedUrl']
        assert dependency.commit == GitMercurialNotationDependency.LATEST_COMMIT
    }

    @Test
    void 'url in map notation should be removed when urls have already been substituted'() {
        // when
        pkg = VcsGolangPackage.builder()
                .withRootPath('github.com/a/b')
                .withPath('github.com/a/b')
                .withSubstitutedVcsInfo(VcsType.GIT, ['url'])
                .build()
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', url: 'specifiedUrl', package: pkg])
        // then
        assert dependency.name == 'github.com/a/b'
        assertEmpty(dependency, 'tag', 'url')
        assert dependency.urls == ['url']
        assert dependency.commit == GitMercurialNotationDependency.LATEST_COMMIT
    }

    @Test
    void 'map notation with version should be parsed correctly'() {
        // when
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', version: '1fc81', package: pkg])
        // then
        assertWithNameAndUrl(dependency)
        assertEmpty(dependency, 'tag')
        assert dependency.commit == '1fc81'
        assert dependency.version == '1fc81'
    }

    @Test
    void 'map notation with commit should be parsed correctly'() {
        // when
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', commit: 'commitId', package: pkg])
        // then
        assertWithNameAndUrl(dependency)
        assertEmpty(dependency, 'tag')
        assert dependency.commit == 'commitId'
        assert dependency.version == 'commitId'
    }

    @Test
    void 'map notation without version should be filled with NEWEST_VERSION'() {
        // when
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', package: pkg])

        // then
        assertEmpty(dependency, 'tag', 'url')
        assert dependency.commit == GitMercurialNotationDependency.LATEST_COMMIT
        assert dependency.version == GitMercurialNotationDependency.LATEST_COMMIT
    }

    @Test
    void 'map notation without packageInfo should not cause an exception'() {
        assert parser.parse([name: 'github.com/a/b', package: pkg])
    }

    @Test
    void 'map notation with unexpected properties should not cause an exception'() {
        // when
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', x: 1, y: 2, package: pkg])

        // then
        assertWithNameAndUrl(dependency)
    }

    @Test
    void 'map notation with extra properties should be set'() {
        // when
        GitMercurialNotationDependency dependency = parser.parse([name: 'github.com/a/b', transitive: false, package: pkg])

        // then
        assert !getExclusionSpecs(dependency).isEmpty()
    }
}
