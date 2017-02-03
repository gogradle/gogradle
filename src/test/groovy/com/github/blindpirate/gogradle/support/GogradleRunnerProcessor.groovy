package com.github.blindpirate.gogradle.support

import org.junit.runners.model.FrameworkMethod

abstract class GogradleRunnerProcessor {
    boolean shouldIgnore(FrameworkMethod method) {
        return false
    }

    abstract void beforeTest(Object instance, FrameworkMethod method)

    abstract void afterTest(Object instance, FrameworkMethod method)
}
