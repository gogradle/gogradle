package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultDependencyVisitorTest {
    @Mock
    ExternalDependencyFactory external1
    @Mock
    ExternalDependencyFactory external2
    @Mock
    SourceCodeDependencyFactory sourceCodeDependencyFactory
    @Mock
    VendorDependencyFactory vendorDependencyFactory
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    GolangDependencySet dependencySet
    @Mock
    DependencyProduceStrategy strategy
    @Mock
    File rootDir

    DefaultDependencyVisitor factory

    @Before
    void setUp() {
        factory = new DefaultDependencyVisitor(
                [external1, external2],
                sourceCodeDependencyFactory,
                vendorDependencyFactory
        )
    }

    @Test
    void 'visiting external dependencies should success'() {
        // given:
        when(external1.produce(rootDir)).thenReturn(Optional.empty())
        when(external2.produce(rootDir)).thenReturn(Optional.of(dependencySet))

        // then:
        assert factory.visitExternalDependencies(resolvedDependency, rootDir) == dependencySet
    }

    @Test
    void 'visiting source dependencies should success'() {
        // given:
        when(sourceCodeDependencyFactory.produce(rootDir)).thenReturn(dependencySet)
        // then:
        assert factory.visitSourceCodeDependencies(resolvedDependency, rootDir) == dependencySet
    }

    @Test
    void 'visiting vendor dependencies should success'() {
        // given:
        when(vendorDependencyFactory.produce(resolvedDependency, rootDir)).thenReturn(GolangDependencySet.empty())
        // then:
        assert factory.visitVendorDependencies(resolvedDependency, rootDir).isEmpty()

        //given:
        when(vendorDependencyFactory.produce(resolvedDependency, rootDir)).thenReturn(dependencySet)
        // then:
        assert factory.visitVendorDependencies(resolvedDependency, rootDir) == dependencySet
    }



}
