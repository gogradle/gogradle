package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key

import static org.mockito.Mockito.*

class MockUtils {
    static void mockVcsService(Class serviceClass, Class annoClass, Object serviceInstance) {
        when(GogradleGlobal.INSTANCE.getInstance(Key.get(serviceClass, annoClass))).thenReturn(serviceInstance)
    }

    static Object mockMutipleInterfaces(Class... interfaceClasses) {
        Assert.isTrue(interfaceClasses.length > 0)
        if (interfaceClasses.length == 1) {
            return mock(interfaceClasses[0])
        } else {
            return mock(interfaceClasses[0], withSettings().extraInterfaces(interfaceClasses[1..-1] as Class[]))
        }
    }

    static VcsGolangPackage mockVcsPackage() {
        return VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withUrl('https://github.com/user/package.git')
                .withVcsType(VcsType.GIT)
                .build()
    }
}
