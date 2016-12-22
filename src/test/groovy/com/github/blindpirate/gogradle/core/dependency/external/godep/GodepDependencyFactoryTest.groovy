package com.github.blindpirate.gogradle.core.dependency.external.godep

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GodepDependencyFactoryTest {
    File resource

    @InjectMocks
    GodepDependencyFactory godepDependencyFactory
    @Mock
    GolangPackageModule module
    @Mock
    MapNotationParser mapNotationParser
    @Mock
    GolangDependency dependency

    @WithResource('')
    @Test
    public void 'package with Godeps/Godeps.json should be rejected'() {
        // given:
        when(module.getRootDir()).thenReturn(resource.toPath())

        // then:
        assert !godepDependencyFactory.produce(module).isPresent()
    }

    @Test
    @WithResource('godep-test.zip')
    public void 'package with Godeps/Godeps.json should be analyzed properly'() {
        // given:
        when(module.getRootDir()).thenReturn(resource.toPath())
        when(mapNotationParser.parse(anyMap())).thenReturn(dependency)
        when(dependency.getName()).thenReturn('name')

        // when:
        def result = godepDependencyFactory.produce(module)
        // then:
        verify(mapNotationParser).parse(eq([name: "github.com/kr/fs", commit: '2788f0dbd16903de03cb8186e5c7d97b69ad387b']))
        verify(mapNotationParser).parse(eq([name  : "github.com/kr/pretty",
                                            commit: 'f31442d60e51465c69811e2107ae978868dbea5c']))

    }
}
