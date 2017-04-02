package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate
import static org.mockito.Mockito.*

class AbstractNotationDependencyTest {
    AbstractNotationDependency depedency = mock(AbstractNotationDependency, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        ReflectionUtils.setField(depedency, 'transitiveDepExclusions', [] as Set)
    }

    @Test
    void 'setting transitive should succeed'() {
        // when
        depedency.setTransitive(false)
        // then
        // exclude any transitive dependencis
        assert depedency.getTransitiveDepExclusions().first().test(null)
    }

    @Test
    void 'exclude some properties should succeed'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(dependency.getName()).thenReturn('a')
        // when
        depedency.exclude([name: 'a'])
        // then
        assert depedency.getTransitiveDepExclusions().first().test(dependency)
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

    @Test
    void 'multiple PropertiesExclusionSpec should be compared properly'() {
        PropertiesExclusionPredicate spec1 = PropertiesExclusionPredicate.of([name: 'name'])
        PropertiesExclusionPredicate spec2 = PropertiesExclusionPredicate.of([name: 'name'] as TreeMap)
        assert spec1.equals(spec2)
        assert spec1.equals(spec1)
        assert !spec1.equals(null)
        assert spec1.hashCode() == spec2.hashCode()
    }
}
