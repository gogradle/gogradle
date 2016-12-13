package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.name.Names
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Matchers.anyMap
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class DefaultMapNotationParserTest {

    @Mock
    DefaultMapNotationParser parser
    @Mock
    MapNotationParser mapNotationParser1
    @Mock
    MapNotationParser mapNotationParser2
    @Mock
    Injector injector
    @Mock
    NotationParser vcsParser

    @Before
    void setUp() {
        parser = new DefaultMapNotationParser([mapNotationParser1, mapNotationParser2])
        VcsType.setInjector(injector)
    }

    @Test
    void 'non map notation should be rejected'() {
        assert !parser.accept({})
    }

    @Test(expected = IllegalStateException)
    void 'nontation without name should be rejected'() {
        parser.produce([vcs: 'git'])
    }

    @Test
    void 'notation with vcs should be delegated to corresponding parser'() {
        // given
        when(injector.getInstance(Key.get(NotationParser.class, Names.named('Svn')))).thenReturn(vcsParser)
        Map notation = [name: 'github.com/a/b', vcs: 'svn']

        // when
        parser.produce(notation)

        // then
        verify(vcsParser).produce(notation)
    }

    @Test
    void 'notation should be delegated to other parser when vcs is absent'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(mapNotationParser1.accept(anyMap())).thenReturn(false)
        when(mapNotationParser2.accept(anyMap())).thenReturn(true)
        when(mapNotationParser2.produce(anyMap())).thenReturn(dependency)

        // then
        assert parser.produce([name: 'github.com/a/b']) == dependency
    }

}
