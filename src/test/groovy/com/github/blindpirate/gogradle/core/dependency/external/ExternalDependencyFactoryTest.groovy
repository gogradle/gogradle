package com.github.blindpirate.gogradle.core.dependency.external

import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser
import org.junit.Before
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@WithResource('')
class ExternalDependencyFactoryTest {
    File resource

    @Mock
    MapNotationParser mapNotationParser
    @Mock
    GolangDependency dependency
    @Mock
    GolangPackageModule module

    @Before
    void superSetUp() {
        when(mapNotationParser.parse(anyMap())).thenReturn(dependency)
        when(dependency.getName()).thenReturn('name')
        when(module.getRootDir()).thenReturn(resource.toPath())
    }

    void verifyMapParsed(Map map) {
        verify(mapNotationParser).parse(eq(map))
    }
}
