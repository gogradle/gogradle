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

package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyManager
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static groovy.util.StringTestUtil.*
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithMockInjector
@WithResource('')
class VendorResolvedDependencyTest {

    @Mock
    VcsResolvedDependency hostDependency
    @Mock
    DependencyManager hostDependencyManager
    @Mock
    DependencyVisitor dependencyVisitor
    @Mock
    VcsAccessor accessor

    VendorResolvedDependency dependency

    ProjectCacheManager projectCacheManager = MockUtils.projectCacheManagerWithoutCache()

    File resource

    @Before
    void setUp() {
        when(hostDependency.getName()).thenReturn('host')
        when(hostDependency.getVersion()).thenReturn('version')
        when(hostDependency.formatVersion()).thenReturn('version')
        when(hostDependency.toString()).thenReturn("host#version")
        when(hostDependency.getVcsType()).thenReturn(VcsType.GIT)

        when(GogradleGlobal.INSTANCE.getInstance(ProjectCacheManager)).thenReturn(projectCacheManager)
        when(GogradleGlobal.INSTANCE.getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(dependencyVisitor.visitVendorDependencies(any(ResolvedDependency), any(File), anyString())).thenReturn(GolangDependencySet.empty())
        when(GogradleGlobal.INSTANCE.getInstance(Key.get(VcsAccessor, Git))).thenReturn(accessor)
        when(hostDependency.getInstaller()).thenReturn(hostDependencyManager)

        IOUtils.mkdir(resource, 'vendor/github.com/a/b')
        dependency = VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b'))
    }


    @Test
    void 'creating a vendor dependency should succeed'() {
        // then
        assert dependency.hostDependency == hostDependency
        assert dependency.dependencies.isEmpty()
        assert toUnixString(dependency.relativePathToHost) == 'vendor/github.com/a/b'
    }

    @Test
    void 'creating a sub vendor dependency of vendor dependency should succeed'() {
        // when
        ReflectionUtils.setField(dependency, 'relativePathToHost', 'vendor/github.com/a/b')
        File dir = IOUtils.mkdir(resource, 'vendor/github.com/a/b/vendor/github.com/b/c')
        VendorResolvedDependency subDependency = VendorResolvedDependency.fromParent('github.com/b/c', dependency, dir)
        // then
        assert subDependency.hostDependency == hostDependency
        assert subDependency.relativePathToHost == 'vendor/github.com/a/b/vendor/github.com/b/c'
    }

    @Test
    void 'formatting a vendor dependency should succeed'() {
        assert dependency.formatVersion() == 'host#version/vendor/github.com/a/b'
    }

    @Test
    void 'update time of vendor resolved dependency in local directory should be the dir\'s last modified time'() {
        // given
        LocalDirectoryDependency hostDependency = LocalDirectoryDependency.fromLocal('root', resource)

        // when
        dependency = VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b'))
        // then
        assert dependency.updateTime == new File(resource, 'vendor/github.com/a/b').lastModified()
        assert dependency.formatVersion() == "root@${toUnixString(resource)}/vendor/github.com/a/b"
    }

    @Test(expected = IllegalStateException)
    void 'host dependency must be local or vcs'() {
        VendorResolvedDependency.fromParent('github.com/a/b', mock(ResolvedDependency), new File(resource, 'vendor/github.com/a/b'))
    }

    @Test
    void 'installing a vendor dependency should succeed'() {
        assert dependency.installer.is(hostDependencyManager)
    }

    @Test
    void 'notation should be generated correctly'() {
        // given
        when(hostDependency.toLockedNotation()).thenReturn([:])
        // then
        assert dependency.toLockedNotation() == [name: 'github.com/a/b', vendorPath: 'vendor/github.com/a/b', host: [:]]

    }

    @Test
    void 'installer class of vendor dependency hosting in LocalDirectoryDependency should be LocalDirectoryDependencyManager'() {
        LocalDirectoryDependencyManager installer = mock(LocalDirectoryDependencyManager)
        when(GogradleGlobal.getInstance(LocalDirectoryDependencyManager)).thenReturn(installer)
        ReflectionUtils.setField(dependency, 'hostDependency', mock(LocalDirectoryDependency))
        assert dependency.getInstaller().is(installer)
    }

    @Test
    void 'equals and hashCode should be correct'() {
        assert dependency.equals(dependency)
        assert !dependency.equals(null)
        assert !dependency.equals(mock(GolangDependency))
        assert dependency == VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b'))
        assert dependency != VendorResolvedDependency.fromParent('github.com/a/c', hostDependency, new File(resource, 'vendor/github.com/a/b'))
        assert dependency.hashCode() == VendorResolvedDependency.fromParent('github.com/a/b', hostDependency, new File(resource, 'vendor/github.com/a/b')).hashCode()
    }

}
