package com.github.blindpirate.gogradle.core.dependency.external.gopm

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy

import static org.mockito.Matchers.anyMap
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GopmDependencyFactoryTest {

    @InjectMocks
    GopmDependencyFactory factory
    @Mock
    MapNotationParser mapNotationParser
    @Spy
    GopmfileParser gopmfileParser
    @Mock
    GolangPackageModule module
    @Mock
    GolangDependency dependency

    File resource

    @Before
    void setUp() {
        when(mapNotationParser.accept(anyMap())).thenReturn(true)
        when(mapNotationParser.parse(anyMap())).thenReturn(dependency)
        when(dependency.getName()).thenReturn("name")
    }

/**
 * github.com/a/b =
 github.com/c/d
 github.com/e/f = /dir/to/my/project
 github.com/g/h = d:\projects\xorm
 golang.org/i/j = branch:master
 golang.org/k/l = tag:v0.9.0
 gopkg.in/redis.v2 = commit:e617904
 */
    @Test
    @WithResource('gopm-test.zip')
    void 'parsing .gopmfile should success'() {
        // given
        when(module.getRootDir()).thenReturn(resource.toPath())

        // when
        factory.produce(module)

        // then
        verify(mapNotationParser).parse(eq([name: 'github.com/a/b']))
        verify(mapNotationParser).parse(eq([name: 'github.com/c/d']))
        verify(mapNotationParser).parse(eq([name: 'github.com/e/f', dir: '/dir/to/my/project']))
        verify(mapNotationParser).parse(eq([name: 'github.com/g/h', dir: /d:\projects\xorm/]))
        verify(mapNotationParser).parse(eq([name: 'golang.org/i/j', branch: 'master']))
        verify(mapNotationParser).parse(eq([name: 'golang.org/k/l', tag: 'v0.9.0']))
        verify(mapNotationParser).parse(eq([name: 'gopkg.in/redis.v2', commit: 'e617904']))

    }

}
