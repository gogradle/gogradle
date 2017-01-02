package com.github.blindpirate.gogradle.core

import com.google.inject.Injector
import org.junit.Before
import org.mockito.Mock

class MockInjectorSupport {
    @Mock
    Injector injector

    @Before
    void superSetup() {
        InjectionHelper.INJECTOR_INSTANCE = injector
    }
}
