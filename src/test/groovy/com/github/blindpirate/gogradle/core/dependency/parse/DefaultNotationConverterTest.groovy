package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.exceptions.PackageResolutionException
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.vcs.Git
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.core.exceptions.PackageResolutionException.cannotResolvePath
import static com.github.blindpirate.gogradle.util.MockUtils.*
import static java.util.Optional.of
import static org.mockito.AdditionalMatchers.not
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultNotationConverterTest extends MockInjectorSupport {
    @Mock
    PackagePathResolver resolver
    @Mock
    NotationConverter gitConverter

    DefaultNotationConverter converter

    @Before
    void setUp() {
        GolangPackage mockedPackage = mockPackage()
        converter = new DefaultNotationConverter(resolver)
        when(resolver.produce('root/package')).thenReturn(of(mockedPackage))
        mockVcsService(injector, NotationConverter, Git, gitConverter)
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

    @Test(expected = PackageResolutionException)
    void 'converting an unrecognized notation should result in an exception'() {
        // given
        when(resolver.produce(not(eq("root/package")))).thenThrow(cannotResolvePath('root/pacakge'))
        // then
        converter.convert('unrecognized')
    }

    @Test(expected = IllegalStateException)
    void 'converting a standard package should result in an exception'() {
        // given
        when(resolver.produce(eq('standard'))).thenReturn(of(mockStandard()))
        // then
        converter.convert('standard')
    }
}
