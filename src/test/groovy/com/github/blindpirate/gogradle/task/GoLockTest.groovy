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
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockResolvedDependency
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GoLockTest extends TaskTest {
    GoLock task

    ResolvedDependency build = mockResolvedDependency('build')
    ResolvedDependency test = mockResolvedDependency('test')

    @Mock
    DependencyTreeNode buildTree
    @Mock
    DependencyTreeNode testTree

    @Before
    void setUp() {
        task = buildTask(GoLock)
    }

    @Test
    void 'lock task should depend on resolve task'() {
        assertTaskDependsOn(task, RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'lock should succeed'() {
        // given
        GolangDependencySet buildSet = asGolangDependencySet(build)
        GolangDependencySet testSet = asGolangDependencySet(test)
        when(golangTaskContainer.get(ResolveBuildDependencies).getDependencyTree()).thenReturn(buildTree)
        when(golangTaskContainer.get(ResolveTestDependenciesDependencies).getDependencyTree()).thenReturn(testTree)
        when(buildTree.flatten()).thenReturn(buildSet)
        when(testTree.flatten()).thenReturn(testSet)
        // when
        task.lock()
        // then
        verify(lockedDependencyManager).lock([build] as Set, [test] as Set)
    }

}
