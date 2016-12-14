package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultMapNotationParserTest {

    DefaultMapNotationParser parser
    @Mock
    MapNotationParser mapNotationParser1
    @Mock
    MapNotationParser mapNotationParser2
    @Mock
    Map notation
    @Mock
    GolangDependency dependency

    @Before
    void setUp() {
        parser = new DefaultMapNotationParser([mapNotationParser1, mapNotationParser2])
    }

    @Test
    void 'non map notation should be rejected'() {
        assert !parser.accept({})
    }

    @Test
    void 'notation should be delegated to corresponding parser'() {
        // given
        when(mapNotationParser1.accept(notation)).thenReturn(false)
        when(mapNotationParser2.accept(notation)).thenReturn(true)
        when(mapNotationParser2.produce(notation)).thenReturn(dependency)

        // then
        assert dependency == parser.produce(notation)
    }

    @Test(expected = DependencyResolutionException)
    void 'notation shoule be rejected when all parsers cannot process it'() {
        //
        when(mapNotationParser1.accept(notation)).thenReturn(false)
        when(mapNotationParser2.accept(notation)).thenReturn(false)

        // then
        parser.produce(notation)
    }

}
