package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.junit.Test

class GolangRepositoryHandlerTest {
    GolangRepositoryHandler handler = new GolangRepositoryHandler()

    @Test
    void 'all other method should throw UnsupportedOperationException'() {

        // We override configure()
        ReflectionUtils.testUnsupportedMethods(handler, RepositoryHandler, ['configure'])
    }
}
