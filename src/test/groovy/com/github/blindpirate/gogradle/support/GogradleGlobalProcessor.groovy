/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
