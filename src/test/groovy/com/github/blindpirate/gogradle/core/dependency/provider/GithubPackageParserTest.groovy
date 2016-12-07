package com.github.blindpirate.gogradle.core.dependency.provider

import org.junit.Test

class GithubPackageParserTest {

    GithubNotationParser provider = new GithubNotationParser();

    @Test
    public void 'parsing name should success'() {
        def result = provider.parse('github.com/a/b')
        assert result.name == 'github.com/a/b'
        assert result.url == 'https://github.com/a/b.git'
    }

    @Test
    public void 'parsing name with tag should success'() {
        def result = provider.parse('github.com/a/b@TAG')
        assert result.name == 'github.com/a/b'
        assert result.url == 'https://github.com/a/b.git'
        assert result.tag == 'TAG'
    }

    @Test
    public void 'parsing name with semversion should success'() {
        def result = provider.parse('github.com/a/b@1.2.3')
        assert result.name == 'github.com/a/b'
        assert result.url == 'https://github.com/a/b.git'
        assert result.semVersion.majorVersion == 1
        assert result.semVersion.minorVersion == 2
        assert result.semVersion.patchVersion == 3
    }

}
