package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryNotationDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class LocalDirectoryResolverTest {

    File resource

    LocalDirectoryResolver resolver = new LocalDirectoryResolver()

    @Mock
    LocalDirectoryNotationDependency dependency

    @Test(expected = IllegalStateException)
    void 'notation with null dir should cause an exception'() {
        resolver.resolve(dependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'notation with invalid dir should cause an exception'() {
        // given
        when(dependency.getDir()).thenReturn("inexistence")
        // then
        resolver.resolve(dependency)
    }

    @Test
    void 'notation with valid dir should be resolved successfully'() {
        // given
        when(dependency.getDir()).thenReturn(resource.absolutePath)
        // then
        assert resolver.resolve(dependency) instanceof LocalDirectoryDependency
    }
}
