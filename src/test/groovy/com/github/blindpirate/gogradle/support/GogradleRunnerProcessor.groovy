package com.github.blindpirate.gogradle.support

import org.junit.runners.model.FrameworkMethod
import org.mockito.Mockito

import java.lang.annotation.Annotation

abstract class GogradleRunnerProcessor<T extends Annotation> {
    boolean shouldIgnore(FrameworkMethod method, T annotation) {
        return false
    }

    void beforeTest(Object instance, FrameworkMethod method, T annotation) {}

    void afterTest(Object instance, FrameworkMethod method, T annotation) {}

    boolean isMock(obj) {
        return obj != null && Mockito.mockingDetails(obj).isMock()
    }
}
