package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.specs.Spec
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class AbstractNotationDependencyTest {
    AbstractNotationDependency abstractNotationDependency = mock(AbstractNotationDependency, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        ReflectionUtils.setField(abstractNotationDependency, 'transitiveDepExclusions', [] as Set)
    }

    Set<Spec> getSpecs() {
        return ReflectionUtils.getField(abstractNotationDependency, 'transitiveDepExclusions')
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
