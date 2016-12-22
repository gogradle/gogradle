package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyProduceStrategy
import com.google.common.base.Optional
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultDependencyFactoryTest {
    @Mock
    DependencyFactory external1
    @Mock
    DependencyFactory external2
    @Mock
    SourceCodeDependencyFactory sourceCodeDependencyFactory
    @Mock
    VendorDependencyFactory vendorDependencyFactory
    @Mock
    GolangPackageModule module
    @Mock
    GolangDependencySet dependencySet
    @Mock
    DependencyProduceStrategy strategy


    DefaultDependencyFactory factory

    @Before
    void setUp() {
        factory = new DefaultDependencyFactory(
                [external1, external2],
                sourceCodeDependencyFactory,
                vendorDependencyFactory
        )
    }


    @Test
    void 'visiting external dependencies should success'() {
        // given:
        when(external1.produce(module)).thenReturn(Optional.absent())
        when(external2.produce(module)).thenReturn(Optional.of(dependencySet))

        // then:
        assert factory.visitExternalDependencies(module).get() == dependencySet
    }

    @Test
    void 'visiting source dependencies should success'() {
        // given:
        when(sourceCodeDependencyFactory.produce(module)).thenReturn(Optional.of(dependencySet))
        // then:
        assert factory.visitSourceCodeDependencies(module) == dependencySet
    }

    @Test
    void 'visiting vendor dependencies should success'() {
        // given:
        when(vendorDependencyFactory.produce(module)).thenReturn(Optional.absent())
        // then:
        assert !factory.visitVendorDependencies(module).isPresent()

        //given:
        when(vendorDependencyFactory.produce(module)).thenReturn(Optional.of(dependencySet))
        // then:
        assert factory.visitVendorDependencies(module).get() == dependencySet
    }

    @Test
    void 'producing should be delegated to strategy'() {
        // given:
        when(module.getProduceStrategy()).thenReturn(strategy)
        when(strategy.produce(module,factory)).thenReturn(dependencySet)

        // when:
        factory.produce(module)

        // then:
        verify(strategy).produce(module, factory)
    }


}
