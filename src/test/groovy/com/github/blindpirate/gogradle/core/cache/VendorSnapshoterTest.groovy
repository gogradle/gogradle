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

package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependencyTest.ResolvedDependencyForTest
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class VendorSnapshoterTest {
    @Mock
    Project project

    File resource

    VendorSnapshoter vendorSnapshoter

    @Before
    void setUp() {
        when(project.getRootDir()).thenReturn(resource)
        vendorSnapshoter = new VendorSnapshoter(project)
        IOUtils.mkdir(resource, 'vendor')
    }

    @Test
    void 'saving and loading cache should succeed'() {
        // given
        ResolvedDependency dependency = new ResolvedDependencyForTest('name', 'version', 1L, null)
        // when
        vendorSnapshoter.updateCache(dependency, new File(resource, 'vendor'))
        vendorSnapshoter.savePersistenceCache()
        ReflectionUtils.getField(vendorSnapshoter, 'cache').clear()
        vendorSnapshoter.loadPersistenceCache()

        // then
        assert ReflectionUtils.getField(vendorSnapshoter, 'cache').get(dependency) instanceof DirectorySnapshot
    }

    @Test
    void 'non-persistence dependency should not be added into cache'() {
        LocalDirectoryDependency dependency = LocalDirectoryDependency.fromLocal('local', resource)
        vendorSnapshoter.updateCache(dependency, new File(resource, 'vendor'))
        assert ReflectionUtils.getField(vendorSnapshoter, 'cache').isEmpty()
    }

    @Test
    void 'directory should be seen as out-of-date if corresponding entry does not exist'() {
        assert !vendorSnapshoter.isUpToDate(mock(ResolvedDependency), new File(resource, 'vendor'))
    }

    @Test
    void 'directory should be seen as up-to-date if it does not change'() {
        // given
        ResolvedDependency dependency = new ResolvedDependencyForTest('name', 'version', 1L, null)
        // when
        vendorSnapshoter.updateCache(dependency, new File(resource, 'vendor'))
        // then
        assert vendorSnapshoter.isUpToDate(dependency, new File(resource, 'vendor'))
    }
}
