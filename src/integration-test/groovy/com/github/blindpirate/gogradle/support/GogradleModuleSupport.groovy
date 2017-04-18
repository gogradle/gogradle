package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before

import static org.mockito.Mockito.RETURNS_DEEP_STUBS
import static org.mockito.Mockito.mock

abstract class GogradleModuleSupport {
    ProjectInternal project = mock(DefaultProject, RETURNS_DEEP_STUBS)

    Injector injector

    @Before
    void initInjector() {
        injector = Guice.createInjector(new GogradleModule(project))
        GogradleGlobal.INSTANCE.setInjector(injector)
        injector.injectMembers(this)
    }
}
