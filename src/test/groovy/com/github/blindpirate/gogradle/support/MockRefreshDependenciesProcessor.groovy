package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

class MockRefreshDependenciesProcessor extends GogradleRunnerProcessor<MockRefreshDependencies> {
    Boolean originalValue

    @Override
    void beforeTest(Object instance, FrameworkMethod method, MockRefreshDependencies annotation) {
        originalValue = ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'refreshDependencies')
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'refreshDependencies', annotation.value())
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, MockRefreshDependencies annotation) {
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'refreshDependencies', originalValue)
    }
}
