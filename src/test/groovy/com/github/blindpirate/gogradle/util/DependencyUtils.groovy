package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.google.common.base.Optional
import org.gradle.api.artifacts.DependencySet

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when;

public class DependencyUtils {
    static GolangDependencySet asGolangDependencySet(GolangDependency... dependencies) {
        return dependencies.inject(new GolangDependencySet(), { ret, dependency ->
            ret.add(dependency)
            return ret
        })
    }

    static Optional<GolangDependencySet> asOptional(GolangDependency... dependencies) {
        if (dependencies.size() == 0) {
            return Optional.absent()
        }
        return Optional.of(asGolangDependencySet(dependencies))
    }

    static DependencySet asDependencySet(GolangDependency... dependencies) {
        return asGolangDependencySet(dependencies).toDependencySet()
    }

    static GolangDependency mockDependency(String name) {
        GolangDependency ret = mock(GolangDependency);
        when(ret.getName()).thenReturn(name)
        return ret
    }
}
