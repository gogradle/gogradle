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
        when(GogradleGlobal.getInstance(ServiceRegistry.class).get(ProgressLoggerFactory.class).newOperation(ProgressMonitorInputStream))
                .thenReturn(logger)
        pmis = new ProgressMonitorInputStream('url', is)
    }

    @Test
    void 'byte count read should be cached'() {
        // when
        while (pmis.read() != -1);
        // then
        verify(logger, times(3)).progress(anyString())
        verify(logger).progress('4 KB downloaded')
        verify(logger, times(2)).progress('8 KB downloaded')
    }

    @Test
    void 'reading after completed should have no side effects'() {
        // when
        while (pmis.read() != -1);
        pmis.read()
        // then
        verify(logger, times(3)).progress(anyString())
        verify(logger).completed()
    }
}
