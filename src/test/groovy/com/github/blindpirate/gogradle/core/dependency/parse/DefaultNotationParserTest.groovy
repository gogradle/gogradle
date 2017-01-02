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
    MapNotationParser mapNotationParser
    @Mock
    NotationConverter notationConverter
    @Mock
    GolangDependency dependency

    DefaultNotationParser parser

    @Before
    void setUp() {
        parser = new DefaultNotationParser(mapNotationParser, notationConverter)
    }

    @Test(expected = DependencyResolutionException)
    void 'parsing unrecognized class should fail'() {
        parser.parse({})
    }

    @Test
    void 'string should be converted to map before parsing'() {
        // given
        String stringNotation = 'notation'
        Map mapNotation = [name: 'notation']
        when(notationConverter.convert(stringNotation)).thenReturn(mapNotation)
        // when
        parser.parse(stringNotation)

        // then
        verify(mapNotationParser).parse(mapNotation)
    }

    @Test
    void 'map parsing should be delegated to MapNotationParser'() {
        // given
        Map notation = [name: 'notation']

        // when
        parser.parse(notation)

        // then
        verify(mapNotationParser).parse(notation)
    }

}
