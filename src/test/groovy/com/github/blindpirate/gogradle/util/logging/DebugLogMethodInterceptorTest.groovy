package com.github.blindpirate.gogradle.util.logging

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.matcher.Matchers
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.internal.logging.sink.OutputEventRenderer
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLogger
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLoggerContext
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito

import static org.mockito.Mockito.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class DebugLogMethodInterceptorTest {
    @Mock
    Logger logger
    @Captor
    ArgumentCaptor<Object> argumentsCaptor
    @Captor
    ArgumentCaptor<String> stringCaptor

    Injector injector = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bindInterceptor(Matchers.any(),
                    Matchers.annotatedWith(DebugLog.class),
                    new DebugLogMethodInterceptor());
        }
    })

    static class LogTest {
        @DebugLog
        public void doNothing() {}

        @DebugLog
        protected void doProtectedNothing() {}

        @DebugLog
        public Object returnNull() { return null }

        @DebugLog
        protected Object returnString() { '' }

        @DebugLog
        public void longTime() {
            Thread.sleep(100)
        }

        @DebugLog
        public Object throwException() {
            throw new IllegalStateException()
        }

        public void withoutAnnotation() {}
    }

    @Before
    void setUp() {
        when(logger.isDebugEnabled()).thenReturn(true)
        ReflectionUtils.setStaticFinalField(new DebugLogMethodInterceptor(), 'LOGGER', logger)
    }

    void verifyDebugLogTwoTimes() {
        verify(logger, times(2)).debug(stringCaptor.capture(), (Object[]) argumentsCaptor.capture())
    }

    void assertEnterAndExit() {
        assert stringCaptor.allValues[0].startsWith('Entering')
        assert stringCaptor.allValues[1].startsWith('Exiting')
    }

    @Test
    void 'doNothing should print debug log'() {
        // when
        injector.getInstance(LogTest).doNothing()
        // then
        verifyDebugLogTwoTimes()
        assertEnterAndExit()
        assert argumentsCaptor.allValues.size() == 6
        assert argumentsCaptor.allValues[0] == 'doNothing'
        assert argumentsCaptor.allValues[3] == 'doNothing'
        assert argumentsCaptor.allValues[5].toDouble() < 10
    }

    @Test
    void 'doProtectedNothing should print debug log'() {
        // when
        injector.getInstance(LogTest).doProtectedNothing()
        // then
        verifyDebugLogTwoTimes()
        assertEnterAndExit()
        assert argumentsCaptor.allValues.size() == 6
        assert argumentsCaptor.allValues[0] == 'doProtectedNothing'
        assert argumentsCaptor.allValues[3] == 'doProtectedNothing'
        assert argumentsCaptor.allValues[5].toDouble() < 10
    }

    @Test
    void 'returnNull should print debug log'() {
        // when
        def result = injector.getInstance(LogTest).returnNull()

        // then
        assert result == null
        verifyDebugLogTwoTimes()
        assertEnterAndExit()
        assert argumentsCaptor.allValues.size() == 6
        assert argumentsCaptor.allValues[0] == 'returnNull'
        assert argumentsCaptor.allValues[3] == 'returnNull'
        assert argumentsCaptor.allValues[5].toDouble() < 10
    }

    @Test
    void 'returnString should print debug log'() {
        // when
        def result = injector.getInstance(LogTest).returnString()
        // then
        assert result == ''
        verifyDebugLogTwoTimes()
        assertEnterAndExit()
        assert argumentsCaptor.allValues.size() == 6
        assert argumentsCaptor.allValues[0] == 'returnString'
        assert argumentsCaptor.allValues[3] == 'returnString'
        assert argumentsCaptor.allValues[5].toDouble() < 10
    }

    @Test
    void 'long time log should be correct'() {
        // when
        injector.getInstance(LogTest).longTime()
        // then
        verifyDebugLogTwoTimes()
        assertEnterAndExit()
        assert argumentsCaptor.allValues.size() == 6
        assert argumentsCaptor.allValues[0] == 'longTime'
        assert argumentsCaptor.allValues[3] == 'longTime'
        assert argumentsCaptor.allValues[5].toDouble() > 100
    }

    @Test
    void 'debug log in method with exception thrown should be correct'() {
        // when
        try {
            injector.getInstance(LogTest).throwException()
            assert false
        } catch (IllegalStateException e) {
        }
        // then
        verifyDebugLogTwoTimes()
        assertEnterAndExit()
        assert argumentsCaptor.allValues.size() == 7
        assert argumentsCaptor.allValues[0] == 'throwException'
        assert argumentsCaptor.allValues[3] == 'throwException'
        assert argumentsCaptor.allValues[5].toDouble() < 10
        assert argumentsCaptor.allValues[6].contains('IllegalStateException')
    }

    @Test
    void 'method without @DebugLog should not be intercepted'() {
        // when
        injector.getInstance(LogTest).withoutAnnotation()
        // then
        verify(logger, times(0)).debug(anyString(), any(Object[].class))
    }


}
