package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.time.Instant

@RunWith(GogradleRunner)
@WithResource('')
class LocalDirectoryDependencyTest {
    File resource

    LocalDirectoryDependency dependency

    @Before
    void setUp() {
        dependency = LocalDirectoryDependency.fromLocal('name', resource)
    }

    @Test(expected = UnsupportedOperationException)
    void 'locking a local dependency should cause an exception'() {
        dependency.toLockedNotation()
    }

    @Test
    void 'version of a local dependency should be its timestamp'() {
        assert Instant.parse(dependency.getVersion()) > Instant.now().minusSeconds(60)
    }

}
