/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.google.inject.Injector
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.internal.service.ServiceRegistry
import org.junit.runners.model.FrameworkMethod

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class WithMockInjectorProcessor extends GogradleGlobalProcessor<WithMockInjector> {
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
        super.beforeTest(instance, method, annotation)
        setUpMockProgressMonitor(GogradleGlobal.INSTANCE.getInjector())
    }
}
