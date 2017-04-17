package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

class MockOfflineProcessor extends GogradleRunnerProcessor<MockOffline> {
    Boolean originalValue

    @Override
    void beforeTest(Object instance, FrameworkMethod method, MockOffline annotation) {
        originalValue = ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'offline')
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', annotation.value())
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, MockOffline annotation) {
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', originalValue)
    }
}
