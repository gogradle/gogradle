package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class AbstractResolvedDependencyTest {
    @Mock
    DependencyInstaller dependencyInstaller

    @Mock
    AbstractResolvedDependency delegate

    File resource

    AbstractResolvedDependency dependency

    @Before
    void setUp() {
        dependency = new ResolvedDependencyForTest('name', 'version', 123L, delegate)
        when(delegate.getInstaller()).thenReturn(dependencyInstaller)
    }

    @Test
    void 'resolved dependency should be resolved to itself'() {
        assert dependency.resolve(null).is(dependency)
    }

    @Test
    void 'toString should succeed'() {
        assert withNameAndVersion('name', 'version').toString() == 'name:version'
    }

    @Test
    @WithResource('')
    void 'version should be recorded in installation directory'() {
        // when
        dependency.installTo(resource)
        // then
        assert new File(resource, '.CURRENT_VERSION').text == 'version'
        verify(dependencyInstaller).install(dependency, resource)
    }

    @Test
    @WithResource('')
    void 'serialization and deserialization should succeed'() {
        // given
        ReflectionUtils.setField(dependency, 'delegate', null)
        dependency.dependencies.add(LocalDirectoryDependency.fromLocal('local', resource))
        dependency.setPackage(MockUtils.mockVcsPackage())

        // when
        IOUtils.serialize(dependency, new File(resource, 'test.bin'))
        ResolvedDependencyForTest result = IOUtils.deserialize(new File(resource, 'test.bin'))

        // then
        assert result.name == 'name'
        assert result.version == 'version'
        assert result.updateTime == 123L
        assert result.dependencies == DependencyUtils.asGolangDependencySet(LocalDirectoryDependency.fromLocal('local', resource))
        assert MockUtils.isMockVcsPackage(result.package)
    }

    AbstractResolvedDependency withNameAndVersion(String name, String version) {
        return new ResolvedDependencyForTest(name, version, 0, delegate)
    }

    static class ResolvedDependencyForTest extends AbstractResolvedDependency {
        private static final long serialVersionUID = 1

        AbstractResolvedDependency delegate

        ResolvedDependencyForTest(String name,
                                  String version,
                                  long updateTime,
                                  AbstractResolvedDependency delegate) {
            super(name, version, updateTime)
            this.delegate = delegate
        }

        @Override
        protected DependencyInstaller getInstaller() {
            return delegate.installer
        }

        @Override
        Map<String, Object> toLockedNotation() {
            return delegate.toLockedNotation()
        }

        @Override
        String formatVersion() {
            return version
        }
    }
}
