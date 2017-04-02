package com.github.blindpirate.gogradle.core.dependency

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


}
