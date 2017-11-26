/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage
import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyManager
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import com.github.blindpirate.gogradle.core.GolangRepositoryPattern
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.time.Instant

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.NO_TRANSITIVE_DEP_PREDICATE
import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate.of
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static com.github.blindpirate.gogradle.util.ReflectionUtils.getField
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class LocalDirectoryDependencyTest {
    File resource

    LocalDirectoryDependency dependency

    @Mock
    LocalDirectoryDependencyManager manager

    @Before
    void setUp() {
        dependency = LocalDirectoryDependency.fromLocal('name', resource)
        when(GogradleGlobal.getInstance(LocalDirectoryDependencyManager)).thenReturn(manager)
    }

    @Test(expected = IllegalStateException)
    void 'rootDir can be set only once'() {
        dependency.setDir(StringUtils.toUnixString(resource))
    }

    @Test
    void 'its cache scope should be build'() {
        assert dependency.cacheScope == CacheScope.BUILD
    }

    @Test
    void 'version format of local directory should be its absolute path'() {
        assert dependency.formatVersion() == StringUtils.toUnixString(resource.toPath().toAbsolutePath())
    }

    @Test
    void 'locking a local dependency should cause an exception'() {
        assert dependency.toLockedNotation() == [name: 'name',
                                                 dir : StringUtils.toUnixString(resource)]
    }

    @Test
    void 'locking a local dependency with subpackages should cause an exception'() {
        dependency.subpackages = ['.'] as Set
        assert dependency.toLockedNotation() == [name       : 'name',
                                                 dir        : StringUtils.toUnixString(resource),
                                                 subpackages: ['.']]
    }

    @Test
    void 'version of a local dependency should be its timestamp'() {
        assert Instant.parse(dependency.getVersion()) > Instant.now().minusSeconds(60)
    }

    @Test
    void 'version of empty local dependency is 0'() {
        LocalDirectoryDependency local = new LocalDirectoryDependency()
        local.dir = GolangRepositoryPattern.EMPTY_DIR
        assert local.updateTime == 0L
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
        LocalDirectoryDependencyManager installer = mock(LocalDirectoryDependencyManager)
        when(GogradleGlobal.INSTANCE.getInstance(LocalDirectoryDependencyManager)).thenReturn(installer)
        // when
        dependency.installTo(dest)
        // then
        verify(installer).install(dependency, dest)
    }

    @Test
    void 'formatting should succeed'() {
        assert dependency.formatVersion() == StringUtils.toUnixString(resource)
    }

    @Test
    void 'resolution and installation should be delegated to LocalDirectoryDependencyManager'() {
        // given
        dependency = new LocalDirectoryDependency()
        dependency.setDir(resource)
        ResolveContext context = mock(ResolveContext)
        // when
        dependency.resolve(context)
        // then
        verify(manager).resolve(context, dependency)
    }

    @Test
    void 'empty dir should be handled properly'() {
        // given
        dependency = new LocalDirectoryDependency()
        dependency.setDir(GolangRepositoryPattern.EMPTY_DIR)

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
        d.dependencies.add(createLocalDirectoryDependency('sub'))
        createVendor(d)
        // when
        LocalDirectoryDependency clone = d.clone()
        // then
        assertLocalDependencyEqual(d, clone, true)
        assertLocalDependencyEqual(
                d.dependencies.find { it.name == 'sub' },
                clone.dependencies.find { it.name == 'sub' }, false)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if cascading descebdant dependencies exist'() {
        LocalDirectoryDependency d = createLocalDirectoryDependency('d')
        LocalDirectoryDependency sub = createLocalDirectoryDependency('sub')
        createVendor(d)
        createVendor(sub)
        d.dependencies.add(sub)
        d.clone()
    }

    @Test
    void 'equals should be correct'() {
        LocalDirectoryDependency d = createLocalDirectoryDependency('d')
        assert d.equals(d)
        assert !d.equals(null)
        assert !d.equals(mock(GolangDependency))
        assert !d.equals(createLocalDirectoryDependency('e'))
        assert d.equals(LocalDirectoryDependency.fromLocal('d', new File(resource, 'd')))
    }

    void assertLocalDependencyEqual(LocalDirectoryDependency old, LocalDirectoryDependency clone, boolean compareVendor) {
        assert !old.is(clone)
        assert old == clone
        assert old.name == clone.name
        assert !getField(old, 'transitiveDepExclusions').is(getField(clone, 'transitiveDepExclusions'))
        assert getField(old, 'transitiveDepExclusions') == getField(clone, 'transitiveDepExclusions')
        assert old.firstLevel == clone.firstLevel

        if (compareVendor) {
            VendorResolvedDependency vendor1InOld = old.dependencies.find { it.name == 'vendor1' }
            VendorResolvedDependency vendor1InClone = clone.dependencies.find { it.name == 'vendor1' }
            VendorResolvedDependency vendor2InOld = vendor1InOld.dependencies.find { it.name == 'vendor2' }
            VendorResolvedDependency vendor2InClone = vendor1InClone.dependencies.find { it.name == 'vendor2' }

            assert vendor1InClone == vendor1InOld
            assert !vendor1InOld.is(vendor1InClone)
            assert vendor2InClone == vendor2InOld
            assert !vendor2InOld.is(vendor2InClone)
            assert vendor1InClone.hostDependency.is(clone)
            assert vendor2InClone.hostDependency.is(clone)
        }
    }

    void createVendor(LocalDirectoryDependency local) {
        VendorResolvedDependency vendor1 = new VendorResolvedDependencyForTest('vendor1', 'version', 1L, local, 'vendor/vendor1')
        VendorResolvedDependency vendor2 = new VendorResolvedDependencyForTest('vendor2', 'version', 2L, local, 'vendor/vendor1/vendor/vendor2')
        vendor1.dependencies.add(vendor2)
        local.dependencies.add(vendor1)
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
