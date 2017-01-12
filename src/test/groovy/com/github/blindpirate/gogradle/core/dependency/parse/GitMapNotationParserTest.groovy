package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.getExclusionSpecs
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GitMapNotationParserTest {
    @Mock
    GolangPackage packageInfo

    GitMapNotationParser parser = new GitMapNotationParser()

    @Before
    void setUp() {
        when(packageInfo.getUrl()).thenReturn('url')
    }

    void assertWithNameAndUrl(GitNotationDependency dependency) {
        assert dependency.name == 'github.com/a/b'
        assert dependency.url == 'url'
    }

    void assertEmpty(GitNotationDependency dependency, String... properties) {
        properties.each {
            assert !dependency[it]
        }
    }

    @Test
    void 'map notation with tag should be parsed correctly'() {
        // when
        GitNotationDependency dependency = parser.parse([name: 'github.com/a/b', tag: 'v1.0.0', package: packageInfo])
        // then
        assertWithNameAndUrl(dependency)
        assertEmpty(dependency, 'commit')
        assert dependency.tag == 'v1.0.0'
    }

    @Test
    void 'url in map notation should not be overwritten'() {
        // when
        GitNotationDependency dependency = parser.parse([name: 'github.com/a/b', url: 'url', package: packageInfo])
        // then
        assertWithNameAndUrl(dependency)
        assertEmpty(dependency, 'tag',)
        assert dependency.url == 'url'
        assert dependency.commit == GitNotationDependency.NEWEST_COMMIT
    }

    @Test
    void 'map notation with version should be parsed correctly'() {
        // when
        GitNotationDependency dependency = parser.parse([name: 'github.com/a/b', version: '1fc81', package: packageInfo])
        // then
        assertWithNameAndUrl(dependency)
        assertEmpty(dependency, 'tag')
        assert dependency.commit == '1fc81'
        assert dependency.version == '1fc81'
    }

    @Test
    void 'map notation with commit should be parsed correctly'() {
        // when
        GitNotationDependency dependency = parser.parse([name: 'github.com/a/b', commit: 'commitId', package: packageInfo])
        // then
        assertWithNameAndUrl(dependency)
        assertEmpty(dependency, 'tag')
        assert dependency.commit == 'commitId'
        assert dependency.version == 'commitId'
    }

    @Test
    void 'map notation without version should be filled with NEWEST_VERSION'() {
        // when
        GitNotationDependency dependency = parser.parse([name: 'github.com/a/b'])

        // then
        assertEmpty(dependency, 'tag', 'url')
        assert dependency.commit == GitNotationDependency.NEWEST_COMMIT
        assert dependency.version == GitNotationDependency.NEWEST_COMMIT
    }

    @Test
    void 'map notation without packageInfo should not cause an exception'() {
        assert parser.parse([name: 'github.com/a/b'])
    }

    @Test
    void 'map notation with unexpected properties should not cause an exception'() {
        // when
        GitNotationDependency dependency = parser.parse([name: 'github.com/a/b', x: 1, y: 2, package: packageInfo])

        // then
        assertWithNameAndUrl(dependency)
    }

    @Test
    void 'map notation with extra properties should be set'() {
        // when
        GitNotationDependency dependency = parser.parse([name: 'github.com/a/b', transitive: false])

        // then
        assert !getExclusionSpecs(dependency).isEmpty()
    }
}
