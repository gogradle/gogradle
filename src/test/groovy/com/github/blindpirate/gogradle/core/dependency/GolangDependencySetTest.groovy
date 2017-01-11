package com.github.blindpirate.gogradle.core.dependency

import org.junit.Test

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockResolvedDependency
import static org.mockito.Mockito.when

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
}
