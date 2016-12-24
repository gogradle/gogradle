package com.github.blindpirate.gogradle.core.infrastructure

import com.github.blindpirate.gogradle.GogradleModule
import com.github.blindpirate.gogradle.core.InjectionHelper
import com.google.inject.Guice
import com.google.inject.Injector
import org.gradle.internal.reflect.Instantiator
import org.junit.Before
import org.mockito.Mock

abstract class GogradleModuleSupport {
    @Mock
    Instantiator instantiator

    Injector injector

    @Before
    void initInjector() {
        injector = Guice.createInjector(new GogradleModule(instantiator))
        InjectionHelper.INJECTOR_INSTANCE = injector
    }
}
