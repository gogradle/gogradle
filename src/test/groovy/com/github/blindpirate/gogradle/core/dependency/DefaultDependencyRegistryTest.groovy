package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultDependencyRegistryTest {
    @Mock
    AbstractResolvedDependency module1
    @Mock
    AbstractResolvedDependency module2

    DefaultDependencyRegistry registry = new DefaultDependencyRegistry()

    @Before
    void setUp() {
        when(module1.getName()).thenReturn("dependency")
        when(module2.getName()).thenReturn("dependency")
        when(module1.isFirstLevel()).thenReturn(false)
        when(module2.isFirstLevel()).thenReturn(false)
    }

    @Test
    void 'dependency should be put at the first time'() {
        assert registry.register(module1)
    }

    @Test
    void 'newer dependency should be put'() {
        // given
        when(module1.getUpdateTime()).thenReturn(1L)
        when(module2.getUpdateTime()).thenReturn(2L)

        // when
        registry.register(module1)

        // then
        assert registry.register(module2)
    }

    @Test
    void 'older dependency should not be put'() {
        // given
        when(module1.getUpdateTime()).thenReturn(2L)
        when(module2.getUpdateTime()).thenReturn(1L)

        // when
        registry.register(module1)

        // then
        assert !registry.register(module2)
    }

    @Test
    void 'first level dependency always win'() {
        // given
        when(module1.getUpdateTime()).thenReturn(2L)
        when(module2.getUpdateTime()).thenReturn(1L)
        // module2 is old but first level
        when(module2.isFirstLevel()).thenReturn(true)

        // when
        registry.register(module1)

        // then
        assert registry.register(module2)
    }
}
