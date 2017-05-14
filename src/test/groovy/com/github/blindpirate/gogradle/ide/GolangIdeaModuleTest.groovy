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

package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import org.gradle.api.Project
import org.gradle.api.internal.TaskInternal
import org.gradle.api.tasks.TaskContainer
import org.gradle.plugins.ide.idea.model.IdeaModule
import org.gradle.plugins.ide.idea.model.IdeaModuleIml
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InOrder
import org.mockito.Mock
import org.mockito.Mockito

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GolangIdeaModuleTest {

    IdeaModule ideaModule = new IdeaModule(mock(Project), mock(IdeaModuleIml))

    GolangIdeaModule golangIdeaModule

    File resource

    @Mock
    TaskContainer taskContainer
    @Mock
    TaskInternal prepareTask
    @Mock
    TaskInternal resolveBuildDependenciesTask
    @Mock
    TaskInternal resolveTestDependenciesTask
    @Mock
    TaskInternal renameVendorTask
    @Mock
    TaskInternal ideaTask


    @Before
    void setUp() {
        IdeaModule.class.fields.each {
            it.setAccessible(true)
            it.set(ideaModule, mock(it.type))
        }
        golangIdeaModule = new GolangIdeaModule(ideaModule)
        when(golangIdeaModule.getProject().getRootDir()).thenReturn(resource)

        when(ideaModule.getProject().getTasks()).thenReturn(taskContainer)
        when(taskContainer.getByName(PREPARE_TASK_NAME)).thenReturn(prepareTask)
        when(taskContainer.getByName(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)).thenReturn(resolveBuildDependenciesTask)
        when(taskContainer.getByName(RESOLVE_TEST_DEPENDENCIES_TASK_NAME)).thenReturn(resolveTestDependenciesTask)
        when(taskContainer.getByName(RENAME_VENDOR_TASK_NAME)).thenReturn(renameVendorTask)
        when(taskContainer.getByName(IDEA_TASK_NAME)).thenReturn(ideaTask)

    }

    @Test
    void 'all fields should be copied'() {
        assert IdeaModule.class.fields.every {
            it.setAccessible(true)
            it.get(golangIdeaModule) == it.get(ideaModule)
        }
    }

    @Test
    @WithResource('')
    void 'task should be executed in order'() {
        // given
        InOrder order = Mockito.inOrder(prepareTask,
                resolveBuildDependenciesTask,
                resolveTestDependenciesTask,
                renameVendorTask,
                ideaTask)
        // when
        golangIdeaModule.resolveDependencies()
        // then
        order.verify(prepareTask).execute()
        order.verify(resolveBuildDependenciesTask).execute()
        order.verify(resolveTestDependenciesTask).execute()
        order.verify(renameVendorTask).execute()
        order.verify(ideaTask).execute()
    }
}
