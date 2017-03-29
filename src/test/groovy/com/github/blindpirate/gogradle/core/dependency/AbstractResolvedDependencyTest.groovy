package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.gradle.api.specs.Spec
import org.junit.Before
import org.junit.Test

import static com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency.PropertiesExclusionSpec
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency

class AbstractResolvedDependencyTest {
    GolangDependency a = mockDependency('a')
    GolangDependency b = mockDependency('b')
    GolangDependency c = mockDependency('c')
    GolangDependency aa = mockDependency('a/a')

    AbstractResolvedDependency dependency = new ResolvedDependencyForTest('', '', 0)

    @Before
    void setUp() {
        dependency.setDependencies(asGolangDependencySet(a, b, c))
    }

    void setSpec(Spec... exclusions) {
        DependencyUtils.setExclusionSpecs(dependency, exclusions as Set)
    }

    @Test
    void 'all dependencies should be excluded when transitive=false'() {
        // given
        Spec excludeAllSpec = new Spec() {
            @Override
            boolean isSatisfiedBy(Object o) {
                return true
            }
        }
        setSpec(excludeAllSpec)

        // then
        assert dependency.getDependencies().isEmpty()
    }

    @Test
    void 'name matched by prefix should be excluded'() {
        // given
        dependency.setDependencies(asGolangDependencySet(aa, b, c))
        // then
        'specific dependencies should be excluded'()
    }

    @Test
    void 'specific dependencies should be excluded'() {
        // given
        setSpec(PropertiesExclusionSpec.of([name: 'a']))
        // then
        assert dependency.getDependencies().size() == 2
        assert dependency.getDependencies().any { it.is(b) }
        assert dependency.getDependencies().any { it.is(c) }
    }

    @Test
    void 'multiple specs should tgake affect'() {
        // given
        setSpec(PropertiesExclusionSpec.of([name: 'a']),
                PropertiesExclusionSpec.of([name: 'b']),
                PropertiesExclusionSpec.of([name: 1]),
                PropertiesExclusionSpec.of([name: 'c', unknown: 'c']))
        // then
        assert dependency.getDependencies().size() == 1
        assert dependency.getDependencies().first().is(c)
    }

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
