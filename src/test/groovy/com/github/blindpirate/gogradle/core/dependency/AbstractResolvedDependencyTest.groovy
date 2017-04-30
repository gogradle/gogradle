package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class AbstractResolvedDependencyTest {
    @Mock
    DependencyManager dependencyManager

    @Mock
    AbstractResolvedDependency delegate

    File resource

    AbstractResolvedDependency dependency

    @Before
    void setUp() {
        dependency = new ResolvedDependencyForTest('name', 'version', 123L, delegate)
        when(delegate.getInstaller()).thenReturn(dependencyManager)
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
    void 'installation should succeed'() {
        // when
        dependency.installTo(resource)
        // then
        verify(dependencyManager).install(dependency, resource)
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

    @Test
    @WithResource('')
    void 'cloning should succeed'() {
        // given
        AbstractResolvedDependency dependency = new ResolvedDependencyForTest('name', 'version', 42L, null)
        dependency.package = MockUtils.mockVcsPackage()
        dependency.dependencies = buildDependencies(dependency)
        // when
        AbstractResolvedDependency clone = dependency.clone()
        // then
        assert clone.class == ResolvedDependencyForTest
        assert !clone.is(dependency)
        assert clone.name == 'name'
        assert clone.version == 'version'
        assert clone.updateTime == 42L
        assert MockUtils.isMockVcsPackage(clone.package)
        assertCloneDependencies(dependency, clone)
    }

    GolangDependency findInDependencies(ResolvedDependency dependency, String name) {
        return dependency.dependencies.find { it.name == name }
    }

    void assertCloneDependencies(ResolvedDependency origin, ResolvedDependency clone) {
        assert origin.dependencies.each { clone.dependencies.contains(it) }
        assert !origin.dependencies.is(clone.dependencies)

        LocalDirectoryDependency local = findInDependencies(clone, 'local')
        assert !local.is(findInDependencies(origin, 'local'))

        VendorResolvedDependency vendor1 = findInDependencies(clone, 'vendor1')
        assert !vendor1.is(findInDependencies(origin, 'vendor1'))
        assert vendor1.hostDependency.is(clone)
        assert vendor1.dependencies.flatten().every() { it.name == 'vendor2' }
        assert vendor1.dependencies.flatten().every() { it.hostDependency.is(clone) }

        NotationDependency notation = findInDependencies(clone, 'notation')
        assert !notation.is(findInDependencies(origin, 'notation'))
        assert ReflectionUtils.getField(notation, 'resolvedDependency') == null
    }

    GolangDependencySet buildDependencies(ResolvedDependency resolvedDependency) {
        LocalDirectoryDependency local = LocalDirectoryDependency.fromLocal('local', resource)

        VendorResolvedDependency vendor1 = new VendorResolvedDependencyForTest('vendor1', 'version', 42L, local, 'vendor/vendor1')
        VendorResolvedDependency vendor2 = new VendorResolvedDependencyForTest('vendor2', 'version', 42L, local, 'vendor/vendor1/vendor/vendor2')

        vendor1.dependencies.add(vendor2)

        NotationDependency notation = new GitNotationDependency()
        notation.setName("notation")
        ReflectionUtils.setField(notation, 'resolvedDependency', Mockito.mock(ResolvedDependency))

        return DependencyUtils.asGolangDependencySet(local, vendor1, notation)
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
        protected DependencyManager getInstaller() {
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
