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

package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.NO_TRANSITIVE_DEP_PREDICATE
import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate
import static com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependencyTest.ResolvedDependencyForTest
import static com.github.blindpirate.gogradle.util.ReflectionUtils.getField
import static com.github.blindpirate.gogradle.util.ReflectionUtils.setField
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class AbstractNotationDependencyTest {
    File resource

    AbstractNotationDependency dependency = mock(AbstractNotationDependency, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        setField(dependency, 'transitiveDepExclusions', [] as Set)
    }

    @Test
    @WithMockInjector
    void 'resolved result should be cached'() {
        // given
        ResolveContext context = mock(ResolveContext)
        DependencyManager resolver = mock(DependencyManager)
        when(resolver.resolve(context, dependency)).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return mock(ResolvedDependency)
            }
        })
        when(GogradleGlobal.INSTANCE.getInstance(DependencyManager)).thenReturn(resolver)
        assert dependency.resolve(context).is(dependency.resolve(context))
    }

    @Test
    void 'setting transitive should succeed'() {
        // when
        dependency.setTransitive(false)
        // then
        // exclude any transitive dependencies
        assert dependency.getTransitiveDepExclusions().first().test(null)
    }

    @Test
    void 'exclude some properties should succeed'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(dependency.getName()).thenReturn('a')
        // when
        this.dependency.exclude([name: 'a'])
        // then
        assert this.dependency.getTransitiveDepExclusions().first().test(dependency)
    }

    @Test
    void 'exclude non-name properties should succeed'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(dependency.getVersion()).thenReturn('version')
        // when
        this.dependency.exclude([version: 'version'])
        // then
        assert this.dependency.getTransitiveDepExclusions().first().test(dependency)
    }

    @Test
    void 'setting package should succeed'() {
        def dependency = new NotationDependencyForTest()
        GolangPackage pkg = MockUtils.mockVcsPackage()
        dependency.package = pkg
        assert dependency.package == pkg
    }

    @Test
    void 'multiple PropertiesExclusionSpec should be compared properly'() {
        PropertiesExclusionPredicate spec1 = PropertiesExclusionPredicate.of([name: 'name'])
        PropertiesExclusionPredicate spec2 = PropertiesExclusionPredicate.of([name: 'name'] as TreeMap)
        assert spec1.equals(spec2)
        assert spec1.equals(spec1)
        assert !spec1.equals(null)
        assert spec1.hashCode() == spec2.hashCode()
    }

    @Test
    @WithResource('')
    void 'serialization and deserialization should succeed'() {
        // given
        AbstractNotationDependency dependency = new NotationDependencyForTest()
        dependency.name = 'name'
        dependency.firstLevel = true
        dependency.exclude([name: 'excludedName', version: 'excludedVersion'])
        dependency.transitive = false
        dependency.package = MockUtils.mockVcsPackage()

        ResolvedDependency resolvedDependency = new ResolvedDependencyForTest('name', 'version', 123L, null)
        resolvedDependency.dependencies.add(LocalDirectoryDependency.fromLocal('local', resource))
        setField(dependency, 'resolvedDependency', resolvedDependency)

        // when
        IOUtils.serialize(dependency, new File(resource, 'out.bin'))
        NotationDependencyForTest result = IOUtils.deserialize(new File(resource, 'out.bin'))
        // then
        assert result.name == 'name'
        assert result.firstLevel
        assert result.transitiveDepExclusions.size() == 2
        assert result.transitiveDepExclusions.contains(PropertiesExclusionPredicate.of([name: 'excludedName', version: 'excludedVersion']))
        assert result.transitiveDepExclusions.contains(NO_TRANSITIVE_DEP_PREDICATE)
        assert MockUtils.isMockVcsPackage(result.package)

        ResolvedDependencyForTest resolvedDependencyInResult = getField(dependency, 'resolvedDependency')
        assert resolvedDependencyInResult.name == 'name'
        assert resolvedDependencyInResult.version == 'version'
        assert resolvedDependencyInResult.updateTime == 123L
        assert resolvedDependencyInResult.dependencies.size() == 1
        assert resolvedDependencyInResult.dependencies.first() == LocalDirectoryDependency.fromLocal('local', resource)
    }

    @Test
    void 'cloning should succeed'() {
        // given
        AbstractNotationDependency dependency = new NotationDependencyForTest()
        dependency.name = 'name'
        dependency.firstLevel = true
        dependency.transitive = false
        dependency.package = MockUtils.mockVcsPackage()
        setField(dependency, 'resolvedDependency', mock(ResolvedDependency))
        // when
        AbstractNotationDependency clone = dependency.clone()
        assert clone.class == NotationDependencyForTest
        assert clone.name == 'name'
        assert clone.firstLevel
        assert MockUtils.isMockVcsPackage(clone.package)
        assert clone.getTransitiveDepExclusions() == [NO_TRANSITIVE_DEP_PREDICATE] as Set
        assert !getField(dependency, 'transitiveDepExclusions').is(getField(clone, 'transitiveDepExclusions'))
        assert getField(clone, 'resolvedDependency') == null
    }

    @Test
    void 'equals should succeed'() {
        AbstractNotationDependency test1 = new NotationDependencyForTest()
        assert !test1.equals(null)
        assert !test1.equals(dependency)
        assert test1 == test1

        PropertiesExclusionPredicate p = PropertiesExclusionPredicate.of([name: ''])

        assert new NotationDependencyForTest('name', false, [NO_TRANSITIVE_DEP_PREDICATE] as Set) == new NotationDependencyForTest('name', false, [NO_TRANSITIVE_DEP_PREDICATE] as Set)
        assert new NotationDependencyForTest('name', true, [] as Set) != new NotationDependencyForTest('name', false, [] as Set)
        assert new NotationDependencyForTest('name', false, [NO_TRANSITIVE_DEP_PREDICATE] as Set) != new NotationDependencyForTest('name', false, [] as Set)
        assert new NotationDependencyForTest('name', false, [null] as Set) != new NotationDependencyForTest('name', false, [] as Set)
        assert new NotationDependencyForTest('name', false, [NO_TRANSITIVE_DEP_PREDICATE] as Set) != new NotationDependencyForTest('name', false, [p] as Set)
    }

    static class NotationDependencyForTest extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        NotationDependencyForTest() {
        }

        NotationDependencyForTest(String name, boolean firstLevel, Set transitiveDepExclusions) {
            setName(name)
            setFirstLevel(firstLevel)
            this.transitiveDepExclusions = transitiveDepExclusions
        }

        @Override
        protected ResolvedDependency doResolve(ResolveContext context) {
            return null
        }

        @Override
        CacheScope getCacheScope() {
            return null
        }
    }
}
