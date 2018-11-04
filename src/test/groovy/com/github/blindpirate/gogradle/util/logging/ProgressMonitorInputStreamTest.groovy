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

package com.github.blindpirate.gogradle.util.logging

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithMockInjector
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.internal.service.ServiceRegistry
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

@WithMockInjector
@RunWith(GogradleRunner)
class ProgressMonitorInputStreamTest {
    @Mock
    ProgressLogger logger

    InputStream is = new ByteArrayInputStream(([0] * 8193) as byte[])

    ProgressMonitorInputStream pmis

    @Before
    void setUp() {
        when(GogradleGlobal.getInstance(ServiceRegistry.class)
                .get(ProgressLoggerFactory.class)
                .newOperation(ProgressMonitorInputStream))
                .thenReturn(logger)
        pmis = new ProgressMonitorInputStream('url', is)
    }

    @Test
    void 'byte count read should be cached'() {
        // when
        while (pmis.read() != -1) {}
        // then
        verify(logger, times(3)).progress(anyString())
        verify(logger).progress('4 KB downloaded')
        verify(logger, times(2)).progress('8 KB downloaded')
    }

    @Test
    void 'reading after completed should have no side effects'() {
        // when
        while (pmis.read() != -1) {}
        pmis.read()
        // then
        verify(logger, times(3)).progress(anyString())
        verify(logger).completed()
    }
}
