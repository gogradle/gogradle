package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.time.Instant

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class LocalDirectoryDependencyTest extends MockInjectorSupport {
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

    @Test
    void 'transitive dependency exclusion should take effect'() {
        // given
        dependency.exclude(name: 'a')
        GolangDependency a = mockDependency('a')
        GolangDependency b = mockDependency('b')

        // when
        dependency.setDependencies(asGolangDependencySet(a, b))

        // then
        assert dependency.dependencies.size() == 1
        assert dependency.dependencies.first().name == 'b'
    }

    @Test
    void 'local dependency should be installed successfully'() {
        // given
        LocalDirectoryDependencyInstaller installer = mock(LocalDirectoryDependencyInstaller)
        File targetDirectory = mock(File)
        when(injector.getInstance(LocalDirectoryDependencyInstaller)).thenReturn(installer)
        // when
        dependency.installTo(targetDirectory)
        // then
        verify(installer).install(dependency, targetDirectory)
    }

    @Test(expected = UnsupportedOperationException)
    void 'local dependency does not support getResolverClass()'(){
        dependency.getResolverClass()
    }

}
