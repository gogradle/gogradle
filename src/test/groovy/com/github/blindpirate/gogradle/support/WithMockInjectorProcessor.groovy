package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.Injector
import org.junit.runners.model.FrameworkMethod

import static org.mockito.Mockito.RETURNS_DEEP_STUBS
import static org.mockito.Mockito.mock

class WithMockInjectorProcessor extends GogradleRunnerProcessor {

    Injector originalValue

    @Override
    void beforeTest(Object instance, FrameworkMethod method) {
        originalValue = ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'injector')
        Injector mockInjector = mock(Injector, RETURNS_DEEP_STUBS)
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'injector', mockInjector)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method) {
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'injector', originalValue)
    }
}
