package com.github.blindpirate.gogradle

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.junit.Test

import java.lang.reflect.InvocationTargetException

class GolangRepositoryHandlerTest {
    GolangRepositoryHandler handler = new GolangRepositoryHandler()

    @Test
    void 'all other method should throw UnsupportedOperationException'() {
        RepositoryHandler.methods.each {
            if (['configure', 'equals', 'hashCode'].contains(it.name) || it.isDefault()) {
                // We override configure()
                return
            }
            try {
                Object[] params = it.getParameterTypes().collect { clazz ->
                    if (clazz.isPrimitive()) {
                        return 0
                    } else {
                        return null
                    }
                }
                it.invoke(handler, params)
                println(it)
                assert false
            } catch (InvocationTargetException e) {
                if (e.cause instanceof NullPointerException) {
                    println(it)
                }
                assert e.cause instanceof UnsupportedOperationException
            }
        }
    }
}
