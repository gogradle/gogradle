package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class LocalFileSystemDependencyTest {
    File resource

    LocalFileSystemDependency dependency

    @Before
    void setUp() {
        dependency = LocalFileSystemDependency.fromLocal('name', resource)
    }

    @Test(expected = UnsupportedOperationException)
    void 'locking a local dependency should cause an exception'() {
        dependency.toLockedNotation()
    }

    @Test(expected = UnsupportedOperationException)
    void 'getting version of a local dependency should cause an exception'() {
        dependency.getVersion()
    }

}
