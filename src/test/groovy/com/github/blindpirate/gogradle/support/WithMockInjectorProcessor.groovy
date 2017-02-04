package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.core.dependency.resolve.LoggerProgressMonitor
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.Injector
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.internal.service.ServiceRegistry
import org.junit.runners.model.FrameworkMethod

import static org.mockito.Mockito.*

class WithMockInjectorProcessor extends GogradleRunnerProcessor<WithMockInjector> {

    Injector originalValue

    void setUpMockProgressMonitor(Injector injector) {
        ProgressLogger mockLogger = mock(ProgressLogger)
        ServiceRegistry mockServiceRegistry = mock(ServiceRegistry)
        ProgressLoggerFactory mockLoggerFactory = mock(ProgressLoggerFactory)
        when(injector.getInstance(ServiceRegistry)).thenReturn(mockServiceRegistry)
        when(mockServiceRegistry.get(ProgressLoggerFactory)).thenReturn(mockLoggerFactory)
        when(mockLoggerFactory.newOperation(LoggerProgressMonitor)).thenReturn(mockLogger)
    }

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithMockInjector annotation) {
        originalValue = ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'injector')
        Injector mockInjector = mock(Injector, RETURNS_DEEP_STUBS)
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'injector', mockInjector)
        setUpMockProgressMonitor(mockInjector)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithMockInjector annotation) {
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'injector', originalValue)
    }
}
