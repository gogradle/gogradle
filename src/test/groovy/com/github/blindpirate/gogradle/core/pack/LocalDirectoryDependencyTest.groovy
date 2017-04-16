package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import com.github.blindpirate.gogradle.vcs.git.GolangRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.time.Instant

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.*
import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate.of
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static com.github.blindpirate.gogradle.util.ReflectionUtils.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class LocalDirectoryDependencyTest {
    File resource

    LocalDirectoryDependency dependency

    @Before
    void setUp() {
        dependency = LocalDirectoryDependency.fromLocal('name', resource)
    }

    @Test
    void 'local directory should be resolved to itself'() {
        // given
        ResolveContext context = mock(ResolveContext)
        // when
        dependency.resolve(context)
        // then
        verify(context).produceTransitiveDependencies(dependency, resource)
    }

    @Test
    void 'version format of local directory should be its absolute path'() {
        assert dependency.formatVersion() == StringUtils.toUnixString(resource.toPath().toAbsolutePath())
    }

    @Test
    void 'locking a local dependency should cause an exception'() {
        assert dependency.toLockedNotation() == [name: 'name', dir: StringUtils.toUnixString(resource)]
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
    void 'dependencies should be set and got successfully'() {
        GolangDependency d = mockDependency('d')
        GolangDependencySet dependencySet = asGolangDependencySet(d)

        dependency.setDependencies(dependencySet)
        assert dependency.getDependencies().is(dependencySet)
    }


    @Test
    @WithResource('')
    void 'local dependency should be installed successfully'() {
        // given
        IOUtils.mkdir(resource, 'src')
        IOUtils.mkdir(resource, 'dest')
        File src = new File(resource, 'src')
        File dest = new File(resource, 'dest')

        dependency = LocalDirectoryDependency.fromLocal('name', src)
        LocalDirectoryDependencyInstaller installer = mock(LocalDirectoryDependencyInstaller)
        when(GogradleGlobal.INSTANCE.getInstance(LocalDirectoryDependencyInstaller)).thenReturn(installer)
        // when
        dependency.installTo(dest)
        // then
        verify(installer).install(dependency, dest)
        assert new File(dest, DependencyInstaller.CURRENT_VERSION_INDICATOR_FILE).text == StringUtils.toUnixString(src)
    }

    @Test(expected = UnsupportedOperationException)
    void 'local dependency does not support getResolverClass()'() {
        dependency.getResolverClass()
    }

    @Test
    void 'formatting should succeed'() {
        assert dependency.formatVersion() == StringUtils.toUnixString(resource)
    }

    @Test
    void 'empty dir should be handled successfully'() {
        // given
        dependency = new LocalDirectoryDependency()
        dependency.setDir(GolangRepository.EMPTY_DIR)

        // then
        assert dependency.resolve(null).is(dependency)
        assert dependency.formatVersion() == ''

        // nothing happens
        dependency.installTo(null)
    }

    @Test
    void 'cloning should succeed'() {
        // given
        LocalDirectoryDependency d = createLocalDirectoryDependency('d')
        d.firstLevel = true
        d.transitive = false
        d.dependencies = asGolangDependencySet(createLocalDirectoryDependency('sub'))
        // when
        LocalDirectoryDependency clone = d.clone()
        // then
        assert !clone.is(d)
        assert clone.dependencies == d.dependencies
        assert !clone.dependencies.is(d.dependencies)
        assert clone.dependencies.first() == d.dependencies.first()
        assert !clone.dependencies.first().is(d.dependencies.first())
        assert clone.name == 'd'
        assert clone.firstLevel
        assert !getField(d, 'transitiveDepExclusions').is(getField(clone, 'transitiveDepExclusions'))
        assert clone.transitiveDepExclusions.size() == 1
        assert clone.transitiveDepExclusions == [NO_TRANSITIVE_DEP_PREDICATE] as Set
    }

    @Test
    void 'serialization and deserialization should succeed'() {
        // given
        LocalDirectoryDependency d1 = createLocalDirectoryDependency('d1')
        LocalDirectoryDependency d2 = createLocalDirectoryDependency('d2')
        LocalDirectoryDependency d3 = createLocalDirectoryDependency('d3')

        d1.dependencies.add(d2)
        d2.dependencies.add(d3)

        d1.exclude([name: 'excluded'])
        d3.transitive = false

        // when
        IOUtils.serialize(d1, new File(resource, 'test.bin'))
        LocalDirectoryDependency resultD1 = IOUtils.deserialize(new File(resource, 'test.bin'))
        LocalDirectoryDependency resultD2 = resultD1.dependencies.first()
        LocalDirectoryDependency resultD3 = resultD2.dependencies.first()
        // then
        assertResultIs(resultD1, 'd1')
        assertResultIs(resultD2, 'd2')
        assertResultIs(resultD3, 'd3')
        assert resultD1.transitiveDepExclusions == [of([name: 'excluded'])] as Set
        assert resultD3.transitiveDepExclusions == [NO_TRANSITIVE_DEP_PREDICATE] as Set
    }

    void assertResultIs(LocalDirectoryDependency localDirectoryDependency, String name) {
        assert localDirectoryDependency.name == name
        assert localDirectoryDependency.rootDir == new File(resource, name)
        assert localDirectoryDependency.package.pathString == name
        assert localDirectoryDependency.package.rootPathString == name
        assert localDirectoryDependency.package.dir == StringUtils.toUnixString(new File(resource, name))
    }

    LocalDirectoryDependency createLocalDirectoryDependency(String name) {
        File rootDir = new File(resource, name)
        assert rootDir.mkdir()
        LocalDirectoryDependency ret = LocalDirectoryDependency.fromLocal(name, rootDir)

        ret.name = name
        ret.package = LocalDirectoryGolangPackage.of(name, name, StringUtils.toUnixString(rootDir))
        return ret
    }
}
