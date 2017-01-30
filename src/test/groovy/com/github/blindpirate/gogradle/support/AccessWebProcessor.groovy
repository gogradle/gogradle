package com.github.blindpirate.gogradle.support

import org.junit.runners.model.FrameworkMethod

class AccessWebProcessor extends GogradleRunnerProcessor {

    @Override
    boolean shouldIgnore(FrameworkMethod method) {
        return System.getProperty('TEST_ARE_OFFLINE')
    }

    @Override
    void beforeTest(Object instance, FrameworkMethod method) {
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method) {
    }
}
