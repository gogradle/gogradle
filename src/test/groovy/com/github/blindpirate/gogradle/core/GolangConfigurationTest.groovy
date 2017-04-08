package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test

class GolangConfigurationTest {
    GolangConfiguration configuration = new GolangConfiguration('build')

    @Test
    void 'dependency registry should be isolated'() {
        GolangConfiguration build = new GolangConfiguration('build')
        GolangConfiguration test = new GolangConfiguration('test')
        assert build.dependencyRegistry instanceof DefaultDependencyRegistry
        assert !build.dependencyRegistry.is(test.dependencyRegistry)
    }

    @Test
    void 'getting dependencies should succeed'() {
        assert configuration.getDependencies().isEmpty()
    }

}
