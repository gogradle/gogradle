package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.core.InjectionHelper
import com.google.inject.Injector
import com.google.inject.Key

import static org.mockito.Mockito.when

class MockUtils {
    static void mockVcsService(Injector injector, Class serviceClass, Class annoClass, Object serviceInstance) {
        InjectionHelper.INJECTOR_INSTANCE = injector
        when(injector.getInstance(Key.get(serviceClass, annoClass))).thenReturn(serviceInstance)
    }
}
