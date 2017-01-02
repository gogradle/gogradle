package com.github.blindpirate.gogradle.core.dependency

import org.junit.Test
import org.mockito.Mockito

import java.util.concurrent.ConcurrentHashMap

import static com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency.*

class AbstractGolangDependencyTest {
    AbstractGolangDependency dependency = Mockito.mock(AbstractGolangDependency, Mockito.CALLS_REAL_METHODS)

    @Test(expected = UnsupportedOperationException)
    void 'copy() should be forbidden'() {
        dependency.copy()
    }

    @Test(expected = UnsupportedOperationException)
    void 'getGroup() should be forbidden'() {
        dependency.getGroup()
    }

    @Test(expected = UnsupportedOperationException)
    void 'getVersion() should be forbidden'() {
        dependency.getVersion()
    }

    @Test(expected = UnsupportedOperationException)
    void 'contentEquals() should be forbidden'() {
        dependency.contentEquals(null)
    }

    @Test
    void 'useless enum test'() {
        assert NoTransitiveSpec.valueOf('NO_TRANSITIVE_SPEC') == NO_TRANSITIVE_DEP_SPEC
        assert NoTransitiveSpec.values().length == 1
    }

    @Test
    void 'multiple PropertiesExclusionSpec should be compared properly'() {
        PropertiesExclusionSpec spec1 = PropertiesExclusionSpec.of([name: 'name'])
        PropertiesExclusionSpec spec2 = PropertiesExclusionSpec.of([name: 'name'] as TreeMap)
        assert spec1.equals(spec2)
        assert spec1.hashCode() == spec2.hashCode()
    }
}
