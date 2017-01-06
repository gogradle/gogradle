package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryNotationDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency
import com.github.blindpirate.gogradle.util.IOUtils
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
    LocalDirectoryNotationDependency notationDependency
    @Mock
    LocalDirectoryDependency resolvedDependency

    @Test(expected = IllegalStateException)
    void 'notation with null dir should cause an exception'() {
        resolver.resolve(notationDependency)
    }

    @Test(expected = DependencyResolutionException)
    void 'notation with invalid dir should cause an exception'() {
        // given
        when(notationDependency.getDir()).thenReturn("inexistence")
        // then
        resolver.resolve(notationDependency)
    }

    @Test
    void 'notation with valid dir should be resolved successfully'() {
        // given
        when(notationDependency.getDir()).thenReturn(resource.absolutePath)
        // then
        assert resolver.resolve(notationDependency) instanceof LocalDirectoryDependency
    }

    @Test
    void 'resetting should succeed'() {
        // given
        File src = IOUtils.mkdir(resource, 'src')
        File dest = IOUtils.mkdir(resource, 'dest')

        when(resolvedDependency.getRootDir()).thenReturn(src)
        IOUtils.write(src, 'main.go', 'This is main.go')
        // when
        resolver.reset(resolvedDependency, dest)
        // then
        assert dest.toPath().resolve('main.go').toFile().getText() == 'This is main.go'

    }
}
