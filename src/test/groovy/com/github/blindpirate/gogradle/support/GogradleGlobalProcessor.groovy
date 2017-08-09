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
import com.github.blindpirate.gogradle.GolangPlugin
import com.google.inject.Injector
import org.gradle.StartParameter
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionContainer
import org.junit.runners.model.FrameworkMethod

import java.lang.annotation.Annotation

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

abstract class GogradleGlobalProcessor<T extends Annotation> extends GogradleRunnerProcessor<T> {
    @Override
    void beforeTest(Object instance, FrameworkMethod method, T annotation) {
        if (!alreadyMocked(GogradleGlobal.INSTANCE.getInjector())) {
            Project project = mockProject()
            GogradleGlobal.INSTANCE.setCurrentProject(project)

            Gradle gradle = mockGradle(project)
            mockStartParameter(gradle)
        }

        doMock(GogradleGlobal.INSTANCE.getInjector().getInstance(Project).gradle.startParameter, annotation)
    }

    Project mockProject() {
        Project project = mock(Project)
        ExtensionContainer extensionContainer = mock(ExtensionContainer)
        Injector injector = mock(Injector)

        when(injector.getInstance(Project)).thenReturn(project)
        when(project.getExtensions()).thenReturn(extensionContainer)
        when(extensionContainer.getByName(GolangPlugin.GOGRADLE_INJECTOR)).thenReturn(injector)
        return project
    }

    StartParameter mockStartParameter(Gradle gradle) {
        StartParameter startParameter = mock(StartParameter)
        when(gradle.getStartParameter()).thenReturn(startParameter)
        return startParameter
    }

    Gradle mockGradle(Project project) {
        Gradle gradle = mock(Gradle)
        when(project.getGradle()).thenReturn(gradle)
        return gradle
    }

    void doMock(StartParameter startParameter, T annotation) {

    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, T annotation) {
        GogradleGlobal.INSTANCE.setCurrentProject(null)
    }
}
