package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import java.util.Optional
import org.gradle.api.artifacts.DependencySet

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when;

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
}
