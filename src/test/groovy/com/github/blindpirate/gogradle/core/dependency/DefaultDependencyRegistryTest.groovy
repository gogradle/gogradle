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
    NotationDependency notationDependency
    @Mock
    AbstractResolvedDependency resolvedDependency1
    @Mock
    AbstractResolvedDependency resolvedDependency2

    DefaultDependencyRegistry registry = new DefaultDependencyRegistry()

    @Before
    void setUp() {
        when(resolvedDependency1.getName()).thenReturn("resolvedDependency")
        when(resolvedDependency2.getName()).thenReturn("resolvedDependency")
        when(resolvedDependency1.isFirstLevel()).thenReturn(false)
        when(resolvedDependency2.isFirstLevel()).thenReturn(false)
    }

    @Test
    void 'dependency should be put at the first time'() {
        assert registry.register(resolvedDependency1)
    }

    @Test
    void 'newer dependency should be put'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(1L)
        when(resolvedDependency2.getUpdateTime()).thenReturn(2L)

        // when
        registry.register(resolvedDependency1)

        // then
        assert registry.register(resolvedDependency2)
    }

    @Test
    void 'older dependency should not be put'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(2L)
        when(resolvedDependency2.getUpdateTime()).thenReturn(1L)

        // when
        registry.register(resolvedDependency1)

        // then
        assert !registry.register(resolvedDependency2)
    }

    @Test
    void 'first level dependency should always win'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(2L)
        when(resolvedDependency2.getUpdateTime()).thenReturn(1L)
        // resolvedDependency2 is old but first level
        when(resolvedDependency2.isFirstLevel()).thenReturn(true)

        // when
        registry.register(resolvedDependency1)
        // then
        assert registry.register(resolvedDependency2)
        assert !registry.register(resolvedDependency1)
    }

    @Test
    void 'same dependency should not be put'() {
        // given
        when(resolvedDependency1.getUpdateTime()).thenReturn(1L)

        // when
        registry.register(resolvedDependency1)

        // then
        assert !registry.register(resolvedDependency1)
    }

    @Test
    void 'resolved dependency should be put into cache'() {
        // when
        registry.putIntoCache(notationDependency, resolvedDependency1)
        // then
        assert registry.getFromCache(notationDependency).get().is(resolvedDependency1)
    }

}
