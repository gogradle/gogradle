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
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class VendorTaskTest extends TaskTest {

    VendorTask task

    File resource

    @Mock
    DependencyTreeNode buildTree
    @Mock
    DependencyTreeNode testTree

    GolangDependencySet buildSet = GolangDependencySet.empty();
    GolangDependencySet testSet = GolangDependencySet.empty();

    @Before
    void setUp() {
        task = buildTask(VendorTask)
        when(project.getRootDir()).thenReturn(resource)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getDependencyTree()).thenReturn(buildTree)
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getDependencyTree()).thenReturn(testTree)

        when(buildTree.flatten()).thenReturn(buildSet)

        when(testTree.flatten()).thenReturn(testSet)
    }

    @Test
    void 'vendor task should depend on resolve task'() {
        assertTaskDependsOn(task, GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'build dependencies should have higher priority'() {
        // given
        LocalDirectoryDependency local = mockWithName(LocalDirectoryDependency, 'a')
        VendorResolvedDependency vendor = mockWithName(VendorResolvedDependency, 'a')
        buildSet.add(local)
        testSet.add(vendor)
        // when
        task.vendor()
        // then
        verify(local).installTo(new File(resource, 'vendor/a'))
        verify(vendor, times(0)).installTo(new File(resource, 'vendor/a'))
    }


    @Test
    void 'vendor should be flattened and cascading vendor should be removed'() {
        // given
        GogradleRootProject root = mock(GogradleRootProject)
        VendorResolvedDependency a = mockVendorResolvedDependency('a', root, 'vendor/a')
        VendorResolvedDependency b = mockVendorResolvedDependency('b', root, 'vendor/a/vendor/b')
        VendorResolvedDependency c = mockVendorResolvedDependency('c', root, 'vendor/a/vendor/b/vendor/c')
        testSet.addAll([a, b, c])

        IOUtils.mkdir(resource, 'vendor/a/vendor/b/vendor/c')
        IOUtils.mkdir(resource, 'vendor/d')

        // when
        task.vendor()
        // then
        verify(a, times(0)).installTo(new File(resource, 'vendor/a'))
        verify(b).installTo(new File(resource, 'vendor/b'))
        verify(c).installTo(new File(resource, 'vendor/c'))
        assert !new File(resource, 'vendor/a/vendor/b/vendor').exists()
        assert !new File(resource, 'vendor/a/vendor').exists()
        assert new File(resource, 'vendor/a').exists()
        assert new File(resource, 'vendor/b').exists()
        assert new File(resource, 'vendor/c').exists()
        assert !new File(resource, 'vendor/d').exists()
    }

    def addDependencyTo(ResolvedDependency resolvedDependency, GolangDependency... dependencies) {
        GolangDependencySet set = asGolangDependencySet(dependencies)
        when(resolvedDependency.getDependencies()).thenReturn(set)
    }

    @Test
    void 'vendor should be reserved if it matched dependency to be installed'() {
        // given
        GogradleRootProject root = mock(GogradleRootProject)
        VendorResolvedDependency a = mockVendorResolvedDependency('a', root, 'vendor/a')
        VendorResolvedDependency b = mockVendorResolvedDependency('b', root, 'vendor/a/vendor/b')
        LocalDirectoryDependency c = mockWithName(LocalDirectoryDependency, 'c')
        testSet.addAll([a, b, c])

        IOUtils.mkdir(resource, 'vendor/a/vendor/b/vendor/c')
        // when
        task.vendor()
        // then
        verify(a, times(0)).installTo(new File(resource, 'vendor/a'))
        verify(b).installTo(new File(resource, 'vendor/b'))
        verify(c).installTo(new File(resource, 'vendor/c'))
        assert !new File(resource, 'vendor/a/vendor/b/vendor').exists()
        assert !new File(resource, 'vendor/a/vendor').exists()
    }

    @Test
    void 'all files under vendor directory should be deleted'() {
        // given
        IOUtils.write(resource, 'vendor/vendor/json', '')
        // when
        task.vendor()
        // then
        assert !new File(resource, 'vendor/vendor/json').exists()
    }
}
