package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class DependencyUtils {
    static GolangDependencySet asGolangDependencySet(GolangDependency... dependencies) {
        return dependencies.inject(new GolangDependencySet(), { ret, dependency ->
            ret.add(dependency)
            return ret
        })
    }

    static GolangDependency mockDependency(String name) {
        GolangDependency ret = mock(GolangDependency);
        when(ret.getName()).thenReturn(name)
        return ret
    }

    static ResolvedDependency mockResolvedDependency(String name) {
        ResolvedDependency ret = mock(ResolvedDependency);
        when(ret.getName()).thenReturn(name)
        when(ret.resolve()).thenReturn(ret)
        return ret
    }

    static Set getExclusionSpecs(AbstractGolangDependency target) {
        return ReflectionUtils.getField(target, 'transitiveDepExclusions')
    }

    static void setExclusionSpecs(AbstractGolangDependency target, Set specs) {
        ReflectionUtils.setField(target, 'transitiveDepExclusions', specs)
    }
}
