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
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.DependencyUtils.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class InstallTaskTest extends TaskTest {

    InstallTask task

    ResolvedDependency resolvedDependency = mockResolvedDependency('name')

    File resource

    @Before
    void setUp() {
        when(project.getProjectDir()).thenReturn(resource)
        task = buildTask(InstallTask)
    }

    /*
   projectRoot
    \--vendor
        |-- github.com
        |   \-- user
        |       |-- a up-to-date
        |       |   \- vendor
        |       |-- b out-of-date
        |       \-- c not exist
        |
        \-- f local directory
     */

    @Test
    void 'installation should succeed'() {
        // given
        ['a', 'b', 'c'].each { IOUtils.write(resource, "vendor/github.com/user/${it}/${it}.go", '') }
        ['f'].each { IOUtils.write(resource, "vendor/${it}/${it}.go", '') }


        ResolvedDependency a = mockResolvedDependency('github.com/user/a')
        ResolvedDependency b = mockResolvedDependency('github.com/user/b')
        when(vendorSnapshoter.isUpToDate(a, new File(resource, 'vendor/github.com/user/a'))).thenReturn(true)
        when(vendorSnapshoter.isUpToDate(b, new File(resource, 'vendor/github.com/user/b'))).thenReturn(false)

        LocalDirectoryDependency f = mockWithName(LocalDirectoryDependency, 'f')
        when(vendorSnapshoter.isUpToDate(f, new File(resource, 'vendor/f'))).thenReturn(false)

        ResolvedDependency b2 = mockResolvedDependency('github.com/user/b')

        GolangDependencySet buildSet = asGolangDependencySet(a, b)
        GolangDependencySet testSet = asGolangDependencySet(b2, f)
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getFlatDependencies()).thenReturn(buildSet)
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getFlatDependencies()).thenReturn(testSet)

        // when
        task.installDependenciesToVendor()

        // then
        verify(a, times(0)).installTo(new File(resource, 'vendor/github.com/user/a'))
        assert !new File(resource, 'vendor/github.com/user/a/vendor').exists()
        assert new File(resource, 'vendor/github.com/user/a/a.go').exists()

        verify(b).installTo(new File(resource, 'vendor/github.com/user/b'))
        verify(b2, times(0)).installTo(new File(resource, 'vendor/github.com/user/b'))
        assert IOUtils.dirIsEmpty(new File(resource, 'vendor/github.com/user/b'))

        assert !new File(resource, 'vendor/github.com/user/c').exists()

        verify(f).installTo(new File(resource, 'vendor/f'))
        assert IOUtils.dirIsEmpty(new File(resource, 'vendor/f'))
    }

    @Test
    void 'files under vendor should be deleted'() {
        // given
        when(golangTaskContainer.get(ResolveBuildDependenciesTask).getFlatDependencies()).thenReturn(GolangDependencySet.empty())
        when(golangTaskContainer.get(ResolveTestDependenciesTask).getFlatDependencies()).thenReturn(GolangDependencySet.empty())
        IOUtils.write(resource, 'vendor/vendor.json', '')
        // when
        task.installDependenciesToVendor()
        // then
        assert !new File(resource, 'vendor/vendor.json').exists()
    }
}
