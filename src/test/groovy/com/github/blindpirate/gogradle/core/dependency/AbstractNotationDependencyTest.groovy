package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.util.DependencyUtils
import org.gradle.api.specs.Spec
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.*

class AbstractNotationDependencyTest {
    AbstractNotationDependency abstractNotationDependency = mock(AbstractNotationDependency, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        DependencyUtils.setExclusionSpecs(abstractNotationDependency, [] as Set)
    }

    Set<Spec> getSpecs() {
        return DependencyUtils.getExclusionSpecs(abstractNotationDependency)
    }

    @Test
    void 'setting transitive should success'() {
        // when
        abstractNotationDependency.setTransitive(false)
        // then
        // exclude any transitive dependencis
        assert getSpecs().first().isSatisfiedBy(null)
    }

    @Test
    void 'exclude some properties should success'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(dependency.getName()).thenReturn('a')
        // when
        abstractNotationDependency.exclude([name: 'a'])
        // then
        assert getSpecs().first().isSatisfiedBy(dependency)
    }
}
