package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.MockUtils
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.*

class AbstractNotationDependencyTest {
    AbstractNotationDependency depedency = mock(AbstractNotationDependency, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        DependencyUtils.setExclusionSpecs(depedency, [] as Set)
    }

    @Test
    void 'setting transitive should succeed'() {
        // when
        depedency.setTransitive(false)
        // then
        // exclude any transitive dependencis
        assert depedency.getTransitiveDepExclusions().first().isSatisfiedBy(null)
    }

    @Test
    void 'exclude some properties should succeed'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(dependency.getName()).thenReturn('a')
        // when
        depedency.exclude([name: 'a'])
        // then
        assert depedency.getTransitiveDepExclusions().first().isSatisfiedBy(dependency)
    }

    @Test
    void 'setting package should succeed'() {
        def dependency = new NotationDependencyForTest()
        GolangPackage pkg = MockUtils.mockVcsPackage()
        dependency.package = pkg
        assert dependency.package == pkg
    }

    static class NotationDependencyForTest extends AbstractNotationDependency {
        private static final long serialVersionUID = 1
        @Override
        protected Class<? extends DependencyResolver> getResolverClass() {
            return null
        }
    }
}
