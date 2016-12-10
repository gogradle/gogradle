package com.github.blindpirate.gogradle.core.dependency.provider

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.parse.GithubNotationParser
import com.google.inject.Injector
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

@RunWith(GogradleRunner)
class GithubPackageParserTest {

    @InjectMocks
    GithubNotationParser provider
    @Mock
    Injector injector


    @Test
    public void 'parsing name should success'() {
        def result = provider.produce('github.com/a/b')
        assert result.name == 'github.com/a/b'
        assert result.url == 'https://github.com/a/b.git'
    }

    @Test
    public void 'parsing name with tag should success'() {
        def result = provider.produce('github.com/a/b@TAG')
        assert result.name == 'github.com/a/b'
        assert result.url == 'https://github.com/a/b.git'
        assert result.tag == 'TAG'
    }

    @Test
    public void 'parsing name with semversion should success'() {
        def result = provider.produce('github.com/a/b@1.2.3')
        assert result.name == 'github.com/a/b'
        assert result.url == 'https://github.com/a/b.git'
        assert result.tag == '1.2.3'
    }

}
