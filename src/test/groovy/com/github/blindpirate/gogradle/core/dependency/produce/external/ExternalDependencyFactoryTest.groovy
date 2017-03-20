package com.github.blindpirate.gogradle.core.dependency.produce.external

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser
import com.github.blindpirate.gogradle.core.pack.StandardPackagePathResolver
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Before
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@WithResource('')
class ExternalDependencyFactoryTest {
    File resource

    @Mock
    MapNotationParser mapNotationParser
    @Mock
    NotationDependency dependency
    @Mock
    AbstractResolvedDependency module
    @Mock
    StandardPackagePathResolver standardPackagePathResolver


    @Before
    void superSetUp() {
        when(mapNotationParser.parse(anyMap())).thenReturn(dependency)
        when(dependency.getName()).thenReturn('name')
        when(standardPackagePathResolver.isStandardPackage(any(Path))).thenReturn(false)
    }

    void verifyMapParsed(Map map) {
        verify(mapNotationParser).parse(eq(map))
    }
}
