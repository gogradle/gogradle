package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

class MockOfflineProcessor extends GogradleRunnerProcessor {
    Boolean originalValue

    @Override
    void beforeTest(Object instance, FrameworkMethod method) {
        originalValue = ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'offline')
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', true)
//        if (ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'offline') == null) {
//            ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', false)
//        }
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method) {
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', originalValue)
    }
}
