package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.time.Instant

import static org.mockito.Mockito.when
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class LocalDirectoryDependencyTest {
    File resource

    LocalDirectoryDependency dependency

    @Before
    void setUp() {
        dependency = LocalDirectoryDependency.fromLocal('name', resource)
    }

    @Test
    void 'local directory should be resolved to itself'() {
        assert dependency.resolve().is(dependency)
    }

    @Test(expected = UnsupportedOperationException)
    void 'locking a local dependency should cause an exception'() {
        dependency.toLockedNotation()
    }

    @Test
    void 'version of a local dependency should be its timestamp'() {
        assert Instant.parse(dependency.getVersion()) > Instant.now().minusSeconds(60)
    }


    @Test(expected = DependencyResolutionException)
    void 'notation with invalid dir should cause an exception'() {
        LocalDirectoryDependency.fromLocal('', new File("inexistence"))
    }

    @Test
    void 'notation with valid dir should be resolved successfully'() {
        LocalDirectoryDependency.fromLocal('', resource)
    }

}
