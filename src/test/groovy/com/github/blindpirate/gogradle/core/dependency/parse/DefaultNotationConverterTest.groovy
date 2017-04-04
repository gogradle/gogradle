package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.vcs.Git
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.MockUtils.mockVcsPackage
import static com.github.blindpirate.gogradle.util.MockUtils.mockVcsService
import static java.util.Optional.of
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithMockInjector
class DefaultNotationConverterTest {
    @Mock
    PackagePathResolver resolver
    @Mock
    NotationConverter gitConverter

    DefaultNotationConverter converter

    @Before
    void setUp() {
        GolangPackage mockedPackage = mockVcsPackage()
        converter = new DefaultNotationConverter(resolver)
        when(resolver.produce('root/package')).thenReturn(of(mockedPackage))
        mockVcsService(NotationConverter, Git, gitConverter)
    }

    @Test
    void 'converting a correct notation should succeed'() {
        // when
        converter.convert('root/package')
        // then
        verify(gitConverter).convert('root/package')
    }

    @Test
    void 'converting notation with separator should succeed'() {
        // when
        converter.convert('root/package#1.0.0')
        // then
        verify(gitConverter).convert('root/package#1.0.0')
    }

    @Test
    void 'unrecognized notation should be converted successfully'() {
        // given
        when(resolver.produce("unrecognized")).thenReturn(of(UnrecognizedGolangPackage.of('unrecognized')))
        // then
        assert converter.convert('unrecognized') == [name: 'unrecognized']
    }
}
