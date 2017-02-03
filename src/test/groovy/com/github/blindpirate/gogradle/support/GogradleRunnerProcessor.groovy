package com.github.blindpirate.gogradle.support

import org.junit.runners.model.FrameworkMethod

abstract class GogradleRunnerProcessor {
    boolean shouldIgnore(FrameworkMethod method) {
        return false
    }

    void beforeTest(Object instance, FrameworkMethod method) {}

    void afterTest(Object instance, FrameworkMethod method) {}
}
