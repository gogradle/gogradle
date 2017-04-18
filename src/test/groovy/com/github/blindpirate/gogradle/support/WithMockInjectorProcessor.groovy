package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.Injector
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.internal.service.ServiceRegistry
import org.junit.runners.model.FrameworkMethod

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

class WithMockInjectorProcessor extends GogradleRunnerProcessor<WithMockInjector> {

    void setUpMockProgressMonitor(Injector injector) {
        ProgressLogger mockLogger = mock(ProgressLogger)
        ServiceRegistry mockServiceRegistry = mock(ServiceRegistry)
        ProgressLoggerFactory mockLoggerFactory = mock(ProgressLoggerFactory)
        when(injector.getInstance(ServiceRegistry)).thenReturn(mockServiceRegistry)
        when(mockServiceRegistry.get(ProgressLoggerFactory)).thenReturn(mockLoggerFactory)
        when(mockLoggerFactory.newOperation((Class) any(Class))).thenReturn(mockLogger)
    }

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithMockInjector annotation) {
        if (!isMock(GogradleGlobal.INSTANCE.getInjector())) {
            GogradleGlobal.INSTANCE.injector = mock(Injector)
        }
        setUpMockProgressMonitor(GogradleGlobal.INSTANCE.getInjector())
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithMockInjector annotation) {
        if (isMock(GogradleGlobal.INSTANCE.getInjector())) {
            reset(GogradleGlobal.INSTANCE.getInjector())
        }
    }
}
