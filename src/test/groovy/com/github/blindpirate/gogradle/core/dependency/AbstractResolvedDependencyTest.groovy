package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import org.junit.Test

class AbstractResolvedDependencyTest {

    AbstractResolvedDependency dependency = new ResolvedDependencyForTest('', '', 0)

    @Test
    void 'resolved dependency should be resolved to itself'() {
        assert dependency.resolve(null).is(dependency)
    }

    @Test
    void 'toString should succeed'() {
        assert withNameAndVersion('name', 'version').toString() == 'name:version'
    }

    AbstractResolvedDependency withNameAndVersion(String name, String version) {
        return new ResolvedDependencyForTest(name, version, 0)
    }

    static class ResolvedDependencyForTest extends AbstractResolvedDependency {
        private static final long serialVersionUID = 1

        protected ResolvedDependencyForTest(String name, String version, long updateTime) {
            super(name, version, updateTime)
        }

        @Override
        protected DependencyInstaller getInstaller() {
            return null
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
