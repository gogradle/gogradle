package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class DefaultStringNotationParserTest {

    @Mock
    StringNotationParser parser1
    @Mock
    StringNotationParser parser2
    @Mock
    GolangDependency dependency

    String notation = 'ThisIsANotation'

    DefaultStringNotationParser parser

    @Before
    void setUp() {
        parser = new DefaultStringNotationParser([parser1, parser2])
    }

    @Test
    void 'non-string notation should be rejected'() {
        assert !parser.accept([:])
    }

    @Test
    void 'parsing string notation should success'() {
        // given
        when(parser1.accept(notation)).thenReturn(false)
        when(parser2.accept(notation)).thenReturn(true)
        when(parser2.produce(notation)).thenReturn(dependency)

        // then
        assert parser.produce(notation) == dependency
    }

    @Test(expected = DependencyResolutionException)
    void 'parsing unknown notation should fail'() {
        // given
        when(parser1.accept(notation)).thenReturn(false)
        when(parser2.accept(notation)).thenReturn(false)

        // then
        parser.produce(notation)

    }
}
