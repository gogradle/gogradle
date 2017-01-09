package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.exceptions.PackageResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static java.util.Optional.empty
import static java.util.Optional.of
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class ErrorReportingPackagePathResolverTest {

    @Mock
    OptionalPackagePathResolver optionalPackagePathResolver

    @Mock
    GolangPackage golangPackage

    ErrorReportingPackagePathResolver resolver

    @Before
    void setUp() {
        resolver = new ErrorReportingPackagePathResolver(optionalPackagePathResolver)
    }

    @Test
    void 'result produced by delegate should be redirected'() {
        // given
        when(optionalPackagePathResolver.produce('root/package')).thenReturn(of(golangPackage))
        // then
        assert resolver.produce('root/package').get().is(golangPackage)
    }

    @Test(expected = PackageResolutionException)
    void 'exception should be thrown if unable to resolve'() {
        // given
        when(optionalPackagePathResolver.produce('root/package')).thenReturn(empty())

        // then
        resolver.produce('root/package')
    }

}
