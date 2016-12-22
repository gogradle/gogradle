package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.resolve.ModuleDependencyVistor
import com.google.common.base.Optional
import org.junit.Before
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.asOptional
import static org.mockito.Mockito.when

abstract class DependencyProduceStrategyTest {
    @Mock
    GolangPackageModule module
    @Mock
    ModuleDependencyVistor visitor

    @Mock
    GolangDependency a1
    @Mock
    GolangDependency b1
    @Mock
    GolangDependency c1
    @Mock
    GolangDependency a2
    @Mock
    GolangDependency b2
    @Mock
    GolangDependency c2

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
        Optional<GolangDependencySet> result = asOptional(dependencies)
        when(visitor.visitVendorDependencies(module)).thenReturn(result)
    }

    void externalDependencies(GolangDependency... dependencies) {
        Optional<GolangDependencySet> set = asOptional(dependencies)
        when(visitor.visitExternalDependencies(module)).thenReturn(set)
    }

    void sourceCodeDependencies(GolangDependency... dependencies) {
        GolangDependencySet set = asGolangDependencySet(dependencies)
        when(visitor.visitSourceCodeDependencies(module)).thenReturn(set)
    }


}
