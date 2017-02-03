package com.github.blindpirate.gogradle.core.dependency.produce.strategy

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.build.Configuration.BUILD
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class DefaultDependencyProduceStrategyTest extends DependencyProduceStrategyTest {
    DefaultDependencyProduceStrategy strategy = new DefaultDependencyProduceStrategy()

    @Test
    void 'source code will be scanned when external and vendor dependencies are all empty'() {
        // given
        externalDependencies()
        vendorDependencies()

        // when
        strategy.produce(resolvedDependency, rootDir, visitor)

        //then
        verify(visitor).visitSourceCodeDependencies(resolvedDependency, rootDir, BUILD)
    }

    @Test
    void 'vendor dependencies should have priority over external dependencies'() {
        // given
        vendorDependencies(a1, b1)
        externalDependencies(a2, c2)

        // when
        def result = strategy.produce(resolvedDependency, rootDir, visitor)
        // then
        assert result.any { it.is(a1) }
        assert result.any { it.is(b1) }
        assert result.any { it.is(c2) }
        assert !result.any { it.is(a2) }
        verify(visitor, times(0)).visitSourceCodeDependencies(resolvedDependency, rootDir, BUILD)
    }

    @Test
    void 'vendor dependencies should be used when external dependencies are empty'() {
        // given
        vendorDependencies(a1)
        externalDependencies()
        // when
        def result = strategy.produce(resolvedDependency, rootDir, visitor)
        // then
        assert result.size() == 1
        assert result.any { it.is(a1) }
        verify(visitor, times(0)).visitSourceCodeDependencies(resolvedDependency, rootDir, BUILD)
    }

    @Test
    void 'external dependencies should be used when vendor are empty'() {
        // given
        vendorDependencies()
        externalDependencies(a2)
        // when
        def result = strategy.produce(resolvedDependency, rootDir, visitor)
        // then
        assert result.size() == 1
        assert result.any { it.is(a2) }
        verify(visitor, times(0)).visitSourceCodeDependencies(resolvedDependency, rootDir, BUILD)
    }

}
