package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.support.WithResource
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

@RunWith(GogradleRunner)
class AbstractResolvedDependencyTest {
    @Mock
    DependencyInstaller dependencyInstaller

    File resource

    AbstractResolvedDependency dependency = new ResolvedDependencyForTest('', '', 0)

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
        Mockito.verify(dependencyInstaller).install(dependency, resource)
    }

    AbstractResolvedDependency withNameAndVersion(String name, String version) {
        return new ResolvedDependencyForTest(name, version, 0)
    }

    static class ResolvedDependencyForTest extends AbstractResolvedDependency {
        private static final long serialVersionUID = 1L

        AbstractResolvedDependency delegate


        protected ResolvedDependencyForTest(String name, String version, long updateTime) {
            super(name, version, updateTime)
        }

        @Override
        protected DependencyInstaller getInstaller() {
            return dependencyInstaller
        }

        @Override
        Map<String, Object> toLockedNotation() {
            return null
        }

        @Override
        String formatVersion() {
            return 'version'
        }
    }
}
