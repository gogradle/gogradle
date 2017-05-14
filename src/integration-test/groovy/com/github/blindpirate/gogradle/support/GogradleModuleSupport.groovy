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
