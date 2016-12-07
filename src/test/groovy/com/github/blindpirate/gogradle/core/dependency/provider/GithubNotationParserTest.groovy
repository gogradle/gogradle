package com.github.blindpirate.gogradle.core.dependency.provider

import com.github.blindpirate.gogradle.core.dependency.GitDependency
import org.junit.Test

class GithubNotationParserTest {

    GithubNotationParser parser = new GithubNotationParser();

    @Test
    public void 'package not starting with github.com should be rejected'() {
        assert !parser.accept('golang.org/a/b')
    }

    @Test
    public void 'package with only name should be parsed correctly'() {
        GitDependency dependency = parser.produce('github.com/a/b')
        assert dependency.name == 'github.com/a/b'
        assert dependency.commit == GitDependency.NEWEST_COMMIT
        assert dependency.url == 'https://github.com/a/b.git'
        assert !dependency.tag
    }

    @Test
    public void 'package with tag should be parsed correctly'() {
        assertPackageWithNameAndTag('github.com/a/b@v1.0.0', 'v1.0.0')
        assertPackageWithNameAndTag('github.com/a/b@v1.0.0-prerelease', 'v1.0.0-prerelease')
        assertPackageWithNameAndTag('github.com/a/b@tag-with@', 'tag-with@')
        assertPackageWithNameAndTag('github.com/a/b@tag-with#', 'tag-with#')
        assertPackageWithNameAndTag('github.com/a/b@tag-contains@1', 'tag-contains@1')
        assertPackageWithNameAndTag('github.com/a/b@tag-contains#1', 'tag-contains#1')
        assertPackageWithNameAndTag('github.com/a/b@tag-contains@and#at-the-same-time', 'tag-contains@and#at-the-same-time')
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
        assertPackageWithNameAndTag('github.com/a/b@1.*', '1.*')
        assertPackageWithNameAndTag('github.com/a/b@1.x', '1.x')
        assertPackageWithNameAndTag('github.com/a/b@~1.5', '~1.5')
        assertPackageWithNameAndTag('github.com/a/b@1.0-2.0', '1.0-2.0')
        assertPackageWithNameAndTag('github.com/a/b@^0.2.3', '^0.2.3')
        assertPackageWithNameAndTag('github.com/a/b@1', '1')
        assertPackageWithNameAndTag('github.com/a/b@!(1.x)', '!(1.x)')
        assertPackageWithNameAndTag('github.com/a/b@~1.3 | (1.4.* & !=1.4.5) | ~2', '~1.3 | (1.4.* & !=1.4.5) | ~2')
    }


    void assertPackageWithNameAndTag(String notation, String tag) {
        GitDependency dependency = parser.produce(notation)
        assert dependency.name == 'github.com/a/b'
        assert dependency.url == 'https://github.com/a/b.git'
        assert dependency.tag == tag
        assert !dependency.commit
    }
}
