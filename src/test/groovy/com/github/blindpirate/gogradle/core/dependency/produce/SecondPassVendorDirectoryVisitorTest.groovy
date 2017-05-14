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

package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.core.pack.UnrecognizedPackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class SecondPassVendorDirectoryVisitorTest {
    PackagePathResolver packagePathResolver = new UnrecognizedPackagePathResolver()
    @Mock
    VcsResolvedDependency hostDependency
    @Mock
    DependencyVisitor dependencyVisitor
    @Mock
    VcsAccessor vcsAccessor

    ProjectCacheManager projectCacheManager = MockUtils.projectCacheManagerWithoutCache()

    File resource

    @Before
    void setUp() {
        when(hostDependency.getVcsType()).thenReturn(VcsType.GIT)
        when(GogradleGlobal.INSTANCE.injector.getInstance(ProjectCacheManager)).thenReturn(projectCacheManager)
        when(GogradleGlobal.INSTANCE.injector.getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(GogradleGlobal.INSTANCE.injector.getInstance(Key.get(VcsAccessor, Git))).thenReturn(vcsAccessor)
        when(vcsAccessor.lastCommitTimeOfPath(any(File), any(Path))).thenReturn(123L)
        IOUtils.write(resource, 'vendor/a/main.go', '')
        IOUtils.write(resource, 'vendor/b/vendor/c/main.go', '')
    }

    @Test
    void 'unrecognized package should be produced correctly'() {
        def visitor = new SecondPassVendorDirectoryVisitor(hostDependency, resource.toPath().resolve('vendor'), packagePathResolver)
        IOUtils.walkFileTreeSafely(resource.toPath().resolve('vendor'), visitor)
        assert visitor.dependencies.size() == 2
        VendorResolvedDependency a = visitor.dependencies.find { it.name == 'a' }
        VendorResolvedDependency b = visitor.dependencies.find { it.name == 'b' }
        [a, b].each {
            assert it instanceof VendorResolvedDependency
            assert it.hostDependency == hostDependency
            assert it.updateTime == 123L
        }
        assert a.relativePathToHost == 'vendor/a'
        assert b.relativePathToHost == 'vendor/b'
    }

}


