package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GitDependency
import com.github.blindpirate.gogradle.core.pack.PackageInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class GitMapNotationParserTest {

    @Mock
    PackageInfo packageInfo
    @Mock
    List<String> urls

    GitMapNotationParser parser = new GitMapNotationParser();

    @Before
    void setUp() {
        when(packageInfo.getUrls()).thenReturn(urls)
    }

    void assertWithNameAndUrls(GitDependency dependency) {
        assert dependency.name == 'github.com/a/b'
        assert dependency.urls == urls
    }

    void assertEmpty(GitDependency dependency, String... properties) {
        properties.each {
            assert !dependency[it]
        }
    }

    @Test
    void 'map notation with tag should be parsed correctly'() {
        // when
        GitDependency dependency = parser.parse([name: 'github.com/a/b', tag: 'v1.0.0', info: packageInfo])
        // then
        assertWithNameAndUrls(dependency)
        assertEmpty(dependency, 'commit', 'url')
        assert dependency.tag == 'v1.0.0'
    }

    @Test
    void 'url in map notation should not be overwrited'() {
        // when
        GitDependency dependency = parser.parse([name: 'github.com/a/b', url: 'url', info: packageInfo])
        // then
        assertWithNameAndUrls(dependency)
        assertEmpty(dependency, 'tag', 'version')
        assert dependency.url == 'url'
    }

    @Test
    void 'map notation with version should be parsed correctly'() {
        // when
        GitDependency dependency = parser.parse([name: 'github.com/a/b', version: '1.0.0', info: packageInfo])
        // then
        assertWithNameAndUrls(dependency)
        assertEmpty(dependency, 'commit', 'url')
        assert dependency.tag == '1.0.0'
        assert dependency.version == '1.0.0'
    }

    @Test
    void 'map notation with commit should be parsed correctly'() {
        // when
        GitDependency dependency = parser.parse([name: 'github.com/a/b', commit: 'commitId', info: packageInfo])
        // then
        assertWithNameAndUrls(dependency)
        assertEmpty(dependency, 'tag', 'version')
        assert dependency.commit == 'commitId'
    }

    @Test
    void 'map notation without version should be filled with NEWEST_VERSION'() {
        // when
        GitDependency dependency = parser.parse([name: 'github.com/a/b'])

        // then
        assertEmpty(dependency, 'tag', 'version', 'url', 'urls')
        assert dependency.commit == GitDependency.NEWEST_COMMIT
    }

    @Test
    void 'map notation without packageInfo should not cause an exception'() {
        assert parser.parse([name: 'github.com/a/b'])
    }

    @Test
    void 'map notation with unexpected properties should not cause an exception'() {
        // when
        GitDependency dependency = parser.parse([name: 'github.com/a/b', x: 1, y: 2, info: packageInfo])

        // then
        assertWithNameAndUrls(dependency)
    }

    @Test
    void 'map notation with extra properties should be set'() {
        // when
        GitDependency dependency = parser.parse([name: 'github.com/a/b', transitive: false])

        // then
        assert !dependency.transitive
    }
}
