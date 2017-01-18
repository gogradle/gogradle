package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.GogradleGlobal
import com.google.inject.Injector
import org.junit.Before
import org.mockito.Mock

class MockInjectorSupport {
    @Mock
    Injector injector

    @Before
    void superSetup() {
        GogradleGlobal.INSTANCE.setInjector(injector)
    }
}
