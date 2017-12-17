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
import com.github.blindpirate.gogradle.GogradleModule
import com.github.blindpirate.gogradle.GolangPlugin
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.google.inject.Guice
import com.google.inject.Injector
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before
import org.mockito.Mock

import static org.mockito.Mockito.*

abstract class GogradleModuleSupport {
    ProjectInternal project = mock(DefaultProject, RETURNS_DEEP_STUBS)

    @Mock
    File projectDir

    Injector injector

    @Before
    void initInjector() {
        when(projectDir.isDirectory()).thenReturn(true)
        when(project.getProjectDir()).thenReturn(projectDir)
        injector = Guice.createInjector(new GogradleModule(project))

        injector.getInstance(GogradleRootProject).setName('github.com/my/project')

        ExtensionContainerInternal extensionContainer = mock(ExtensionContainerInternal)
        when(project.getExtensions()).thenReturn(extensionContainer)

        when(extensionContainer.getByName(GolangPlugin.GOGRADLE_INJECTOR)).thenReturn(injector)
        GogradleGlobal.INSTANCE.setCurrentProject(project)
        injector.injectMembers(this)
    }
}
