package com.github.blindpirate.gogradle.core.dependency.provider

import com.github.blindpirate.gogradle.core.dependency.parse.GitNotationConverter
import org.junit.Test

class GitNotationConverterTest {

    GitNotationConverter converter = new GitNotationConverter()

    @Test
    public void 'package with only name should be parsed correctly'() {
        assert converter.convert('github.com/a/b') == [name: 'github.com/a/b']
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
        assert converter.convert('github.com/a/b#commitId') == [name: 'github.com/a/b', commit: 'commitId']
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
        assert converter.convert(notation) == [name: 'github.com/a/b', tag: tag]
    }
//
//    void assertWithNameAndUrl(GitDependency dependency) {
//        assert dependency.name == 'github.com/a/b'
//        assert dependency.url == 'https://github.com/a/b.git'
//    }
//
//    @Test
//    public void 'map notation with tag should be parsed correctly'() {
//        GitDependency dependency = converter.parse([name: 'github.com/a/b', tag: 'v1.0.0'])
//        assertWithNameAndUrl(dependency)
//        assert dependency.tag == 'v1.0.0'
//        assert !dependency.commit
//    }
//
//    @Test
//    public void 'map notation with version should be parsed correctly'() {
//        GitDependency dependency = converter.parse([name: 'github.com/a/b', version: '1.0.0'])
//        assertWithNameAndUrl(dependency)
//        assert dependency.tag == '1.0.0'
//        assert dependency.version == '1.0.0'
//        assert !dependency.commit
//    }
//
//    @Test
//    public void 'map notation with commit should be parsed correctly'() {
//        GitDependency dependency = converter.parse([name: 'github.com/a/b', commit: 'commitId'])
//        assertWithNameAndUrl(dependency)
//        assert dependency.commit == 'commitId'
//        assert !dependency.tag
//        assert !dependency.version
//    }
//
//    @Test
//    public void 'map notation with url should be parsed correctly'() {
//        GitDependency dependency = converter.parse([name: 'github.com/a/b', url: 'git@github.com:a/b.git'])
//        assert dependency.name == 'github.com/a/b'
//        assert dependency.url == 'git@github.com:a/b.git'
//        assert !dependency.tag
//        assert !dependency.version
//        assert dependency.commit == GitDependency.NEWEST_COMMIT
//    }
//
//    @Test
//    public void 'map notation with extra properties should be set'() {
//        GitDependency dependency = converter.parse(
//                [name      : 'github.com/a/b',
////                 excludeVendor: true,
//                 transitive: true]
//        )
//
//        assertWithNameAndUrl(dependency)
//        assert dependency.transitive
////        assert dependency.excludeVendor
//    }


}
