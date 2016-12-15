package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackageModule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultDependencyRegistryTest {
    @Mock
    GolangDependency dependency1
    @Mock
    GolangDependency dependency2
    @Mock
    GolangPackageModule module1
    @Mock
    GolangPackageModule module2

    DefaultDependencyRegistry registry = new DefaultDependencyRegistry()

    @Before
    void setUp() {
        when(dependency1.getName()).thenReturn("dependency")
        when(dependency2.getName()).thenReturn("dependency")
        when(dependency1.getPackage()).thenReturn(module1)
        when(dependency2.getPackage()).thenReturn(module2)
        when(dependency1.isFirstLevel()).thenReturn(false)
        when(dependency2.isFirstLevel()).thenReturn(false)
    }

    @Test
    void 'dependency should be put at the first time'() {
        assert registry.registry(dependency1)
    }

    @Test
    void 'newer dependency should be put'() {
        // given
        when(module1.getUpdateTime()).thenReturn(1L)
        when(module2.getUpdateTime()).thenReturn(2L)

        // when
        registry.registry(dependency1)

        // then
        assert registry.registry(dependency2)
    }

    @Test
    void 'older dependency should not be put'(){
        // given
        when(module1.getUpdateTime()).thenReturn(2L)
        when(module2.getUpdateTime()).thenReturn(1L)

        // when
        registry.registry(dependency1)

        // then
        assert !registry.registry(dependency2)
    }

    @Test
    void 'first level dependency always win'(){
        // given
        when(module1.getUpdateTime()).thenReturn(2L)
        when(module2.getUpdateTime()).thenReturn(1L)
        // module2 is old but first level
        when(dependency2.isFirstLevel()).thenReturn(true)

        // when
        registry.registry(dependency1)

        // then
        assert registry.registry(dependency2)
    }
}
