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

package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DependenciesTaskTest extends TaskTest {
    DependenciesTask task

    @Mock
    DependencyTreeNode buildTree
    @Mock
    DependencyTreeNode testTree
    @Mock
    Logger logger

    @Before
    void setUp() {
        task = buildTask(DependenciesTask)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(buildTree)
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getDependencyTree()).thenReturn(testTree)
        when(buildTree.output()).thenReturn('build output')
        when(testTree.output()).thenReturn('test output')
        ReflectionUtils.setStaticFinalField(DependenciesTask, 'LOGGER', logger)
    }

    @After
    void cleanUp() {
        ReflectionUtils.setStaticFinalField(DependenciesTask, 'LOGGER', Logging.getLogger(DependenciesTask))
    }

    @Test
    void 'dependencies task should depends on resolve task'(){
        assertTaskDependsOn(task,RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task,RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'dependency tree should be displayed correctly'() {
        // when
        task.displayDependencies()
        // then
        verify(logger).quiet('build:')
        verify(logger).quiet('test:')
        verify(logger).quiet('build output')
        verify(logger).quiet('test output')
    }

}
