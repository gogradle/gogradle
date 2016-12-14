package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class DefaultNotationParserTest {

    @Mock
    NotationParser parser1
    @Mock
    NotationParser parser2
    @Mock
    GolangDependency dependency

    DefaultNotationParser parser

    @Before
    void setUp() {
        parser = new DefaultNotationParser([parser1, parser2]);
    }

    @Test(expected = DependencyResolutionException)
    public void 'parsing unrecognized class should fail'() {
        parser.produce({})
    }

    @Test
    public void 'parsing a notation should success'() {
        // given
        when(parser1.accept(any())).thenReturn(false)
        when(parser2.accept(any())).thenReturn(true)
        when(parser2.produce(any())).thenReturn(dependency)

        // then
        assert parser.produce(new Object()) == dependency

    }

}
