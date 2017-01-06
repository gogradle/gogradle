package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.dependency.resolve.LocalDirectoryResolver
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class LocalDirectoryNotationDependencyTest extends MockInjectorSupport {
    LocalDirectoryNotationDependency dependency = new LocalDirectoryNotationDependency()

    @Mock
    LocalDirectoryResolver localFileResolver

    @Test
    void 'a LocalDirectoryNotationDependency should be resolved by LocalFileResolver'() {
        // given
        when(injector.getInstance(LocalDirectoryResolver)).thenReturn(localFileResolver)
        // when
        dependency.resolve()
        // then
        verify(localFileResolver).resolve(dependency)
    }
}
