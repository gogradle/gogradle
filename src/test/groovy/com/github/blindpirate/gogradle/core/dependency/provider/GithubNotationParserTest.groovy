package com.github.blindpirate.gogradle.core.dependency.provider

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GitDependency
import com.github.blindpirate.gogradle.core.dependency.parse.GithubNotationParser
import com.google.inject.Injector
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

@RunWith(GogradleRunner)
class GithubNotationParserTest {

    @InjectMocks
    GithubNotationParser parser
    @Mock
    Injector injector


    @Test
    public void 'package not starting with github.com should be rejected'() {
        assert !parser.accept('golang.org/a/b')
    }

    @Test
    public void 'map notation not containing github.com should be rejected'() {
        assert !parser.accept([name: 'golang.org/a/b'])
        assert !parser.accept([:])
    }

    @Test
    public void 'package with only name should be parsed correctly'() {
        GitDependency dependency = parser.produce('github.com/a/b')
        assertNewestCommit(dependency)
    }

    @Test
    public void 'map notation with only name should success'() {
        GitDependency dependency = parser.produce([name: 'github.com/a/b'])
        assertNewestCommit(dependency)
    }

    private void assertNewestCommit(GitDependency dependency) {
        assert dependency.name == 'github.com/a/b'
        assert dependency.commit == GitDependency.NEWEST_COMMIT
        assert dependency.url == 'https://github.com/a/b.git'
        assert !dependency.tag
    }

    @Test
    public void 'package with tag should be parsed correctly'() {
        assertWithTag('github.com/a/b@v1.0.0', 'v1.0.0')
        assertWithTag('github.com/a/b@v1.0.0-prerelease', 'v1.0.0-prerelease')
        assertWithTag('github.com/a/b@tag-with@', 'tag-with@')
        assertWithTag('github.com/a/b@tag-with#', 'tag-with#')
        assertWithTag('github.com/a/b@tag-contains@1', 'tag-contains@1')
        assertWithTag('github.com/a/b@tag-contains#1', 'tag-contains#1')
        assertWithTag('github.com/a/b@tag-contains@and#at-the-same-time', 'tag-contains@and#at-the-same-time')
    }

    @Test
    public void 'package with commit should be parsed correctly'() {
        GitDependency dependency = parser.produce('github.com/a/b#commitId')
        assert dependency.name == 'github.com/a/b'
        assert dependency.url == 'https://github.com/a/b.git'
        assert dependency.commit == 'commitId'
        assert !dependency.tag
    }

    // https://github.com/zafarkhaja/jsemver
    @Test
    public void 'package with sem version should be parsed correctly'() {
        assertWithTag('github.com/a/b@1.*', '1.*')
        assertWithTag('github.com/a/b@1.x', '1.x')
        assertWithTag('github.com/a/b@~1.5', '~1.5')
        assertWithTag('github.com/a/b@1.0-2.0', '1.0-2.0')
        assertWithTag('github.com/a/b@^0.2.3', '^0.2.3')
        assertWithTag('github.com/a/b@1', '1')
        assertWithTag('github.com/a/b@!(1.x)', '!(1.x)')
        assertWithTag('github.com/a/b@~1.3 | (1.4.* & !=1.4.5) | ~2', '~1.3 | (1.4.* & !=1.4.5) | ~2')
    }

    void assertWithTag(String notation, String tag) {
        GitDependency dependency = parser.produce(notation)
        assert dependency.name == 'github.com/a/b'
        assert dependency.url == 'https://github.com/a/b.git'
        assert dependency.tag == tag
        assert !dependency.commit
    }

    void assertWithNameAndUrl(GitDependency dependency) {
        assert dependency.name == 'github.com/a/b'
        assert dependency.url == 'https://github.com/a/b.git'
    }

    @Test
    public void 'map notation with tag should be parsed correctly'() {
        GitDependency dependency = parser.produce([name: 'github.com/a/b', tag: 'v1.0.0'])
        assertWithNameAndUrl(dependency)
        assert dependency.tag == 'v1.0.0'
        assert !dependency.commit
    }

    @Test
    public void 'map notation with version should be parsed correctly'() {
        GitDependency dependency = parser.produce([name: 'github.com/a/b', version: '1.0.0'])
        assertWithNameAndUrl(dependency)
        assert dependency.tag == '1.0.0'
        assert dependency.version == '1.0.0'
        assert !dependency.commit
    }

    @Test
    public void 'map notation with commit should be parsed correctly'() {
        GitDependency dependency = parser.produce([name: 'github.com/a/b', commit: 'commitId'])
        assertWithNameAndUrl(dependency)
        assert dependency.commit == 'commitId'
        assert !dependency.tag
        assert !dependency.version
    }

    @Test
    public void 'map notation with url should be parsed correctly'() {
        GitDependency dependency = parser.produce([name: 'github.com/a/b', url: 'git@github.com:a/b.git'])
        assert dependency.name == 'github.com/a/b'
        assert dependency.url == 'git@github.com:a/b.git'
        assert !dependency.tag
        assert !dependency.version
        assert dependency.commit == GitDependency.NEWEST_COMMIT
    }

    @Test
    public void 'map notation with extra properties should be set'() {
        GitDependency dependency = parser.produce(
                [name         : 'github.com/a/b',
//                 excludeVendor: true,
                 transitive   : true]
        )

        assertWithNameAndUrl(dependency)
        assert dependency.transitive
//        assert dependency.excludeVendor
    }


}
