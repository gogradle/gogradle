package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleModule
import com.github.blindpirate.gogradle.GogradleGlobal
import com.google.inject.Guice
import com.google.inject.Injector
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.junit.Before
import org.mockito.Mock

import static org.mockito.Mockito.RETURNS_DEEP_STUBS
import static org.mockito.Mockito.mock

abstract class GogradleModuleSupport {
    @Mock
    Instantiator instantiator

    ProjectInternal project = mock(ProjectInternal, RETURNS_DEEP_STUBS)

    Injector injector

    @Before
    void initInjector() {
        injector = Guice.createInjector(new GogradleModule(project, instantiator))
        GogradleGlobal.INSTANCE.setInjector(injector)
        injector.injectMembers(this)
    }
}
