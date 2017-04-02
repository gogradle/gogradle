package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency
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
        GolangDependency ret = mock(GolangDependency)
        when(ret.getName()).thenReturn(name)
        return ret
    }

    static mockWithName(Class clazz, String name) {
        def ret = mock(clazz)
        when(ret.getName()).thenReturn(name)
        return ret
    }

    static ResolvedDependency mockResolvedDependency(String name) {
        ResolvedDependency ret = mock(ResolvedDependency)
        when(ret.getName()).thenReturn(name)
        when(ret.formatVersion()).thenReturn('version')
        when(ret.toString()).thenReturn(name)
        when(ret.resolve()).thenReturn(ret)
        return ret
    }

    static Set getExclusionSpecs(AbstractNotationDependency target) {
        return ReflectionUtils.getField(target, 'transitiveDepExclusions')
    }

}
