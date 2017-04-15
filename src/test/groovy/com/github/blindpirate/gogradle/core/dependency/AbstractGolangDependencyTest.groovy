package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.util.MockUtils
import org.junit.Test
import org.mockito.Mockito

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
    void 'cloning should succeed'() {
        // given
        AbstractGolangDependency dependency = new AbstractGolangDependency() {
            @Override
            ResolvedDependency resolve(ResolveContext context) {
                return null
            }
        }
        dependency.name = 'name'
        dependency.package = MockUtils.mockVcsPackage()
        dependency.firstLevel = true

        // when
        AbstractGolangDependency clone = dependency.clone()
        // then
        assert clone.name == 'name'
        assert clone.firstLevel
        assert MockUtils.isMockVcsPackage(dependency.package)
    }
}
