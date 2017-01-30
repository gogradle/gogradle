package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.junit.Test

class GolangDependencyHandlerTest {
    GolangDependencyHandler handler = new GolangDependencyHandler(null, null)

    @Test
    void 'unsupported method should all throw UnsupportedException'() {
        ReflectionUtils.testUnsupportedMethods(handler, DependencyHandler, ['create', 'add'])
    }
}
