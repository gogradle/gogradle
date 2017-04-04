package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.GolangConfigurationManager
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.function.Predicate

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate.of
import static com.github.blindpirate.gogradle.util.DependencyUtils.*
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithMockInjector
class ResolveContextTest {
    GolangDependency a = mockDependency('a')
    GolangDependency b = mockDependency('b')
    GolangDependency c = mockDependency('c')
    GolangDependency aa = mockDependency('a/a')

    GolangDependencySet dependencySet = asGolangDependencySet(a, b, c)

    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    NotationDependency notationDependency
    @Mock
    GolangConfiguration buildConfiguration
    @Mock
    GolangConfiguration testConfiguration
    @Mock
    DependencyProduceStrategy strategy
    @Mock
    GolangConfigurationManager configurationManager

    ResolveContext root

    @Before
    void setUp() {
        root = ResolveContext.root(buildConfiguration)
        ReflectionUtils.setField(root, 'dependencyProduceStrategy', strategy)

        when(buildConfiguration.getName()).thenReturn('build')
        when(GogradleGlobal.INSTANCE.getInjector().getInstance(DependencyVisitor)).thenReturn(mock(DependencyVisitor))
        when(GogradleGlobal.INSTANCE.getInjector().getInstance(GolangConfigurationManager)).thenReturn(configurationManager)
        when(configurationManager.getByName('build')).thenReturn(buildConfiguration)
        when(configurationManager.getByName('test')).thenReturn(testConfiguration)
        when(strategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor), anyString()))
                .thenReturn(dependencySet)
    }

    @Test
    void 'getting dependency registry should be delegated to configutaion'() {
        // when
        when(buildConfiguration.getDependencyRegistry()).thenReturn(mock(DependencyRegistry))
        // then
        assert root.dependencyRegistry == buildConfiguration.dependencyRegistry
    }

    @Test
    void 'root context should allow all dependencies'() {
        assert root.produceTransitiveDependencies(resolvedDependency, mock(File)) == asGolangDependencySet(a, b, c)
    }

    void setExclusion(ResolveContext context, Predicate... predicates) {
        ReflectionUtils.setField(context, 'transitiveDepExclusions', predicates as Set)
    }

    @Test
    void 'all dependencies should be excluded when transitive=false'() {
        // given
        setExclusion(root, AbstractNotationDependency.NO_TRANSITIVE_DEP_PREDICATE)

        // then
        assert root.produceTransitiveDependencies(resolvedDependency, mock(File)).isEmpty()
    }

    @Test
    void 'name matched by prefix should be excluded'() {
        // given
        dependencySet.clear()
        dependencySet.addAll([aa, b, c])
        // then
        'specific dependencies should be excluded'()
    }

    @Test
    void 'specific dependencies should be excluded'() {
        // given
        setExclusion(root, of([name: 'a']))
        // then
        assert root.produceTransitiveDependencies(resolvedDependency, mock(File)) == asGolangDependencySet(b, c)
    }

    @Test
    void 'multiple specs should take affect'() {
        // given
        setExclusion(root, of([name: 'a']),
                of([name: 'b']),
                of([name: 1]),
                of([name: 'c', unknown: 'c']))
        // then
        assert root.produceTransitiveDependencies(resolvedDependency, mock(File)) == asGolangDependencySet(c)
    }

    @Test
    void 'creating sub context should succeed'() {
        // given
        Predicate mockPredicate = mock(Predicate)
        when(notationDependency.getTransitiveDepExclusions()).thenReturn([mockPredicate] as Set)
        // when
        ResolveContext sub1 = root.createSubContext(notationDependency)
        ResolveContext sub2 = root.createSubContext(resolvedDependency)
        // then
        assert sub1.parent == root
        assert sub1.configuration == buildConfiguration
        assert sub1.dependencyProduceStrategy == root.dependencyProduceStrategy
        assert sub1.transitiveDepExclusions == [mockPredicate] as Set

        assert sub2.parent == root
        assert sub2.configuration == buildConfiguration
        assert sub2.dependencyProduceStrategy == root.dependencyProduceStrategy
        assert sub2.transitiveDepExclusions == [] as Set
    }

    @Test
    void 'exclusion predicates should be inherited from root'() {
        // given
        setExclusion(root, of([name: 'a']))
        when(notationDependency.getTransitiveDepExclusions()).thenReturn([of([name: 'b'])] as Set)
        ResolveContext sub = root.createSubContext(notationDependency)
        ReflectionUtils.setField(sub, 'dependencyProduceStrategy', strategy)
        // then
        assert sub.produceTransitiveDependencies(resolvedDependency, mock(File)) == asGolangDependencySet(c)
    }

    @Test
    void 'vendor resolved dependency should be excluded recursively'() {
        // given
        VendorResolvedDependency vendor = mockWithName(VendorResolvedDependency, 'vendor')
        VendorResolvedDependency vendorSub = mockWithName(VendorResolvedDependency, 'vendorSub')
        VendorResolvedDependency vendorSubSub = mockWithName(VendorResolvedDependency, 'vendorSubSub')

        [vendor, vendorSub, vendorSubSub].each {
            when(it.getDependencies()).thenCallRealMethod()
            when(it.setDependencies(any(GolangDependencySet))).thenCallRealMethod()
        }

        vendor.setDependencies(asGolangDependencySet(vendorSub))
        vendorSub.setDependencies(asGolangDependencySet(vendorSubSub))

        setExclusion(root, of([name: 'vendorSubSub']))

        dependencySet.clear()
        dependencySet.add(vendor)
        // when
        GolangDependencySet result = root.produceTransitiveDependencies(resolvedDependency, mock(File))
        assert result == asGolangDependencySet(vendor)
        assert result.first().dependencies == asGolangDependencySet(vendorSub)
        assert result.first().dependencies.first().dependencies.empty
    }
}
