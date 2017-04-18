package com.github.blindpirate.gogradle.core.dependency.produce.strategy

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import org.junit.Before
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static org.mockito.Mockito.when

abstract class DependencyProduceStrategyTest {
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    File rootDir
    @Mock
    DependencyVisitor visitor

    @Mock
    AbstractGolangDependency a1
    @Mock
    AbstractGolangDependency b1
    @Mock
    AbstractGolangDependency c1
    @Mock
    AbstractGolangDependency a2
    @Mock
    AbstractGolangDependency b2
    @Mock
    AbstractGolangDependency c2

    @Before
    void superSetup() {
        when(a1.getName()).thenReturn('a')
        when(b1.getName()).thenReturn('b')
        when(c1.getName()).thenReturn('c')
        when(a2.getName()).thenReturn('a')
        when(b2.getName()).thenReturn('b')
        when(c2.getName()).thenReturn('c')
    }

    void vendorDependencies(GolangDependency... dependencies) {
        GolangDependencySet result = asGolangDependencySet(dependencies)
        when(visitor.visitVendorDependencies(resolvedDependency, rootDir, 'build')).thenReturn(result)
    }

    void externalDependencies(GolangDependency... dependencies) {
        GolangDependencySet set = asGolangDependencySet(dependencies)
        when(visitor.visitExternalDependencies(resolvedDependency, rootDir, 'build')).thenReturn(set)
    }

    void sourceCodeDependencies(GolangDependency... dependencies) {
        GolangDependencySet set = asGolangDependencySet(dependencies)
        when(visitor.visitSourceCodeDependencies(resolvedDependency, rootDir, 'build')).thenReturn(set)
    }


}
