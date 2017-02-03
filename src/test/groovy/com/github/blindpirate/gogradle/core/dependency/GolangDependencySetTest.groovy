package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Action
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.specs.Spec
import org.junit.Test


import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockResolvedDependency
import static org.mockito.Mockito.*

class GolangDependencySetTest {

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

    @Test
    void 'all methods of facade should be delegated to itself'() {
        GolangDependencySet set = new GolangDependencySet()
        DependencySet facade = set.toDependencySet()
        GolangDependencySet mockDependencySet = mock(GolangDependencySet)
        ReflectionUtils.setField(facade, 'this$0', mockDependencySet)

        facade.withType(String)
        verify(mockDependencySet).withType(String)

        Action action = mock(Action)
        facade.withType(String, action)
        verify(mockDependencySet).withType(String, action)

        Closure closure = mock(Closure)
        facade.withType(String, closure)
        verify(mockDependencySet).withType(String, closure)

        Spec spec = mock(Spec)
        facade.matching(spec)
        verify(mockDependencySet).matching(spec)

        facade.matching(closure)
        verify(mockDependencySet).matching(closure)

        facade.whenObjectAdded(action)
        verify(mockDependencySet).whenObjectAdded(action)

        facade.whenObjectAdded(closure)
        verify(mockDependencySet).whenObjectAdded(closure)

        facade.whenObjectRemoved(action)
        verify(mockDependencySet).whenObjectRemoved(action)

        facade.whenObjectRemoved(closure)
        verify(mockDependencySet).whenObjectRemoved(closure)

        facade.all(action)
        verify(mockDependencySet).all(action)

        facade.all(closure)
        verify(mockDependencySet).all(closure)

        facade.findAll(closure)
        verify(mockDependencySet).findAll(closure)

        facade.isEmpty()
        verify(mockDependencySet).isEmpty()

        facade.contains('')
        verify(mockDependencySet).contains('')

        facade.toArray()
        verify(mockDependencySet).toArray()

        Object[] array = [] as Object[]
        facade.toArray(array)
        verify(mockDependencySet).toArray(array)

        facade.remove('')
        verify(mockDependencySet).remove('')

        facade.containsAll([])
        verify(mockDependencySet).containsAll([])

        facade.removeAll([])
        verify(mockDependencySet).removeAll([])

        facade.retainAll([])
        verify(mockDependencySet).retainAll([])

        facade.addAll([])
        verify(mockDependencySet).addAll([])

        facade.clear()
        verify(mockDependencySet).clear()
    }

    @Test(expected = UnsupportedOperationException)
    void 'exception should be thrown when invoking some methods'() {
        new GolangDependencySet().toDependencySet().getBuildDependencies()
    }
}
