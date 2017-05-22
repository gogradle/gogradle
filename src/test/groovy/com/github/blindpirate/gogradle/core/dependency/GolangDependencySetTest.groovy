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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.junit.Test
import org.junit.runner.RunWith

import java.lang.reflect.Method

import static com.github.blindpirate.gogradle.util.DependencyUtils.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class GolangDependencySetTest {

    File resource

    @Test
    void 'collector should be correct'() {
        GolangDependencySet set1 = new GolangDependencySet()
        GolangDependencySet set2 = new GolangDependencySet()
        LocalDirectoryDependency dependency = new LocalDirectoryDependency()
        dependency.setName('local')
        set1.add(dependency)

        GolangDependencySet combined = GolangDependencySet.COLLECTOR.combiner().apply(set1, set2)
        assert combined.size() == 1
        assert combined.first().is(dependency)
    }

    @Test
    void 'flattening should succeed'() {
        // given
        ResolvedDependency d1 = mockResolvedDependency('d1')
        ResolvedDependency d2 = mockResolvedDependency('d2')
        ResolvedDependency d3 = mockResolvedDependency('d3')
        GolangDependencySet set1 = asGolangDependencySet(d1)
        GolangDependencySet set2 = asGolangDependencySet(d2)
        GolangDependencySet set3 = asGolangDependencySet(d3)
        // when
        when(d1.getDependencies()).thenReturn(set2)
        when(d2.getDependencies()).thenReturn(set3)
        when(d3.getDependencies()).thenReturn(GolangDependencySet.empty())
        // then
        assert set1.flatten() as Set == [d1, d2, d3] as Set
    }

    @Test(expected = IllegalStateException)
    void 'flattening should fail if max recursion depth is reached'() {
        // given
        ResolvedDependency resolvedDependency = mockResolvedDependency('resolvedDependency')
        GolangDependencySet set = asGolangDependencySet(resolvedDependency)
        when(resolvedDependency.getDependencies()).thenReturn(set)
        // when
        set.flatten()
    }

    void assertUnsupport(Closure closure) {
        try {
            closure.call()
        }
        catch (UnsupportedOperationException e) {
            return
        }
        assert false
    }

    @Test
    void 'all methods should be delegated to container'() {
        GolangDependencySet set = GolangDependencySet.empty()
        TreeSet container = mock(TreeSet)
        ReflectionUtils.setField(set, 'container', container)

        reset(container)
        set.size()
        verify(container).size()

        reset(container)
        set.isEmpty()
        verify(container).isEmpty()

        reset(container)
        set.contains(0)
        verify(container).contains(0)

        reset(container)
        set.iterator()
        verify(container).iterator()

        reset(container)
        set.toArray()
        verify(container).toArray()

        reset(container)
        int[] array = [] as int[]
        set.toArray(array)
        verify(container).toArray(array)

        reset(container)
        set.add(null)
        verify(container).add(null)

        reset(container)
        set.remove(1)
        verify(container).remove(1)

        reset(container)
        set.containsAll([])
        verify(container).containsAll([])

        reset(container)
        set.addAll([])
        verify(container).addAll([])

        reset(container)
        set.retainAll([])
        verify(container).retainAll([])
    }

    @Test
    void 'dependencies should be identified by names'() {
        GolangDependency d1 = DependencyUtils.mockWithName(GolangDependency, 'name')
        GolangDependency d2 = DependencyUtils.mockWithName(GolangDependency, 'name')

        GolangDependencySet set = GolangDependencySet.empty()
        set.add(d1)
        set.add(d2)

        assert set.size() == 1
        assert set.contains(d1)
        assert set.contains(d2)
    }

    @Test
    void 'equals and hashCode should succeed'() {
        GolangDependencySet set1 = GolangDependencySet.empty()
        GolangDependencySet set2 = GolangDependencySet.empty()

        assert callEqualsViaReflection(set1, set1)
        assert callEqualsViaReflection(set1, set2)
        assert !callEqualsViaReflection(set1, null)
        assert !callEqualsViaReflection(set1, [])
        assert set1.hashCode() == set2.hashCode()

        GolangDependency d = DependencyUtils.mockWithName(GolangDependency, 'name')
        set1.add(d)
        set2.add(d)

        assert callEqualsViaReflection(set1, set2)
        assert set1.hashCode() == set2.hashCode()
    }

    boolean callEqualsViaReflection(GolangDependencySet set1, Object set2) {
        Method m = GolangDependencySet.class.getMethod('equals', Object)
        m.invoke(set1, [set2] as Object[])
    }


    @Test
    @WithResource('')
    void 'serialization and deserialization should succeed'() {
        // given
        File output = new File(resource, 'output.bin')
        GolangDependencySet set = buildADependencySet()

        // when
        IOUtils.serialize(set, output)
        def result = IOUtils.deserialize(output)

        // then
        assert result instanceof GolangDependencySet
        verifyResult(result)
    }

    @Test
    @WithResource('')
    void 'cloning should succeed'() {
        // when
        GolangDependencySet oldSet = buildADependencySet()
        GolangDependencySet newSet = oldSet.clone()

        // then
        assert oldSet == newSet
        assert !oldSet.is(newSet)
        assert !findInDependencies(oldSet, GitNotationDependency).is(findInDependencies(newSet, GitNotationDependency))
        assert !findInDependencies(oldSet, VendorNotationDependency).is(findInDependencies(newSet, VendorNotationDependency))
        assert !findInDependencies(oldSet, LocalDirectoryDependency).is(findInDependencies(newSet, LocalDirectoryDependency))
        verifyResult(newSet)
    }

    @Test
    void 'removeAll should succeed'() {
        GolangDependency a = mockDependency('a')
        GolangDependency b1 = mockDependency('b')
        GolangDependency b2 = mockDependency('b')
        GolangDependency c = mockDependency('c')
        assert asGolangDependencySet(b1).removeAll([a, b2])
        assert asGolangDependencySet(a, b1).removeAll([b2])
        assert !asGolangDependencySet(a, b1).removeAll([c])
    }

    def findInDependencies(GolangDependencySet set, Class clazz) {
        return set.find { it.class == clazz }
    }

    private void verifyResult(GolangDependencySet result) {
        def gitDependency = result.find { it instanceof GitNotationDependency }
        assert gitDependency.name == 'gitDependency'
        assert gitDependency.commit == 'commit'
        assert gitDependency.urls == ['url']
        assert gitDependency.firstLevel

        def vendorDependency = result.find { it instanceof VendorNotationDependency }
        assert vendorDependency.name == 'vendorDependency'
        assert vendorDependency.hostNotationDependency.commit == 'commit'
        assert vendorDependency.hostNotationDependency.urls == ['url']
        assert vendorDependency.hostNotationDependency.firstLevel
        assert vendorDependency.vendorPath == 'vendor/path'

        def localDependency = result.find { it instanceof LocalDirectoryDependency }
        assert localDependency.name == 'resource'
        assert localDependency.rootDir == resource
    }

    private GolangDependencySet buildADependencySet() {
        GolangDependencySet set = new GolangDependencySet()
        set.add(newGitDependency())
        set.add(LocalDirectoryDependency.fromLocal('resource', resource))
        set.add(newVendorDependency())
        set
    }

    VendorNotationDependency newVendorDependency() {
        VendorNotationDependency ret = new VendorNotationDependency()
        ret.name = 'vendorDependency'
        ret.hostNotationDependency = newGitDependency()
        ret.vendorPath = 'vendor/path'
        return ret
    }

    GitNotationDependency newGitDependency() {
        GitNotationDependency ret = new GitNotationDependency()
        ret.name = 'gitDependency'
        ret.commit = 'commit'
        ret.url = 'url'
        ret.firstLevel = true
        return ret
    }
}
