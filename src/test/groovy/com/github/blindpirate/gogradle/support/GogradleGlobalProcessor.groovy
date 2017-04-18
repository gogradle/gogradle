package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.google.inject.Injector
import org.gradle.StartParameter
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.junit.runners.model.FrameworkMethod

import java.lang.annotation.Annotation

import static org.mockito.Mockito.*

abstract class GogradleGlobalProcessor<T extends Annotation> extends GogradleRunnerProcessor<T> {
    @Override
    void beforeTest(Object instance, FrameworkMethod method, T annotation) {
        if (!isMock(GogradleGlobal.INSTANCE.getInjector())) {
            GogradleGlobal.INSTANCE.injector = mock(Injector)
        }

        Project project = mockProject()
        Gradle gradle = mockGradle(project)
        StartParameter startParameter = mockStartParameter(gradle)

        doMock(startParameter, annotation)
    }

    StartParameter mockStartParameter(Gradle gradle) {
        StartParameter startParameter = mock(StartParameter)
        when(gradle.getStartParameter()).thenReturn(startParameter)
        return gradle.getStartParameter()
    }

    Gradle mockGradle(Project project) {
        Gradle gradle = mock(Gradle)
        when(project.getGradle()).thenReturn(gradle)
        return project.getGradle()
    }

    abstract void doMock(StartParameter startParameter, T annotation)

    Project mockProject() {
        Project project = mock(Project)
        when(GogradleGlobal.INSTANCE.getInstance(Project)).thenReturn(project)
        return GogradleGlobal.INSTANCE.getInstance(Project)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, T annotation) {
        if (isMock(GogradleGlobal.INSTANCE.getInjector())) {
            reset(GogradleGlobal.INSTANCE.getInjector())
        }
    }
}
