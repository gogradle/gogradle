package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.util.DependencyUtils
import org.gradle.api.specs.Spec
import org.junit.Before
import org.junit.Test

import static com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency.*
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.Mockito.CALLS_REAL_METHODS
import static org.mockito.Mockito.mock

class AbstractResolvedDependencyTest {
    GolangDependency a = mockDependency('a')
    GolangDependency b = mockDependency('b')
    GolangDependency c = mockDependency('c')

    AbstractResolvedDependency dependency = mock(AbstractResolvedDependency, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        dependency.setDependencies(asGolangDependencySet(a, b, c))
    }

    void setSpec(Spec... exclusions) {
        DependencyUtils.setExclusionSpecs(dependency, exclusions as Set)
    }

    @Test
    void 'all dependencies should be excluded when transitive=false'() {
        // given
        Spec excludeAllSpec = new Spec() {
            @Override
            boolean isSatisfiedBy(Object o) {
                return true
            }
        }
        setSpec(excludeAllSpec)

        // then
        assert dependency.getDependencies().isEmpty()
    }

    @Test
    void 'specific dependencies should be excluded'() {
        // given
        setSpec(PropertiesExclusionSpec.of([name: 'a']))
        // then
        assert dependency.getDependencies().size() == 2
        assert dependency.getDependencies().any { it.is(b) }
        assert dependency.getDependencies().any { it.is(c) }
    }

    @Test
    void 'multiple specs should tgake affect'() {
        // given
        setSpec(PropertiesExclusionSpec.of([name: 'a']),
                PropertiesExclusionSpec.of([name: 'b']),
                PropertiesExclusionSpec.of([name: 1]),
                PropertiesExclusionSpec.of([name: 'c', unknown: 'c']))
        // then
        assert dependency.getDependencies().size() == 1
        assert dependency.getDependencies().first().is(c)
    }
}
