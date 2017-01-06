package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.InjectionHelper
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Injector
import com.google.inject.Key

import static org.mockito.Mockito.*

class MockUtils {
    static void mockVcsService(Injector injector, Class serviceClass, Class annoClass, Object serviceInstance) {
        InjectionHelper.INJECTOR_INSTANCE = injector
        when(injector.getInstance(Key.get(serviceClass, annoClass))).thenReturn(serviceInstance)
    }

    static Object mockMutipleInterfaces(Class... interfaceClasses) {
        Assert.isTrue(interfaceClasses.length > 0)
        if (interfaceClasses.length == 1) {
            return mock(interfaceClasses[0])
        } else {
            return mock(interfaceClasses[0], withSettings().extraInterfaces(interfaceClasses[1..-1] as Class[]))
        }
    }

    static GolangPackage mockPackage() {
        return GolangPackage.builder()
                .withPath('root/package')
                .withRootPath('root')
                .withStandard(false)
                .withVcsType(VcsType.GIT)
                .build()
    }

    static GolangPackage mockStandard() {
        return GolangPackage.builder()
                .withPath('standard')
                .withRootPath('standard')
                .withVcsType(VcsType.GIT)
                .withStandard(true)
                .build()
    }
}
