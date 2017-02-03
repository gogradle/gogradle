package com.github.blindpirate.gogradle.core.dependency.produce.external

import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
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
    NotationDependency dependency
    @Mock
    AbstractResolvedDependency module

    @Before
    void superSetUp() {
        when(mapNotationParser.parse(anyMap())).thenReturn(dependency)
        when(dependency.getName()).thenReturn('name')
    }

    void verifyMapParsed(Map map) {
        verify(mapNotationParser).parse(eq(map))
    }
}
