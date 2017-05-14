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

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.apache.commons.collections4.map.LRUMap
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class ResolveDependencyToDependenciesCacheTest {
    File resource

    @Mock
    PackagePathResolver resolver
    @Mock
    Project project
    @Mock
    DependencyVisitor dependencyVisitor

    ProjectCacheManager projectCacheManager = MockUtils.projectCacheManagerWithoutCache()

    PersistenceResolvedToDependenciesCache cache

    @Before
    void setUp() {
        when(GogradleGlobal.INSTANCE.getInstance(Project)).thenReturn(project)
        when(GogradleGlobal.INSTANCE.getInstance(ProjectCacheManager)).thenReturn(projectCacheManager)
        when(GogradleGlobal.getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(dependencyVisitor.visitVendorDependencies(any(ResolvedDependency), any(File), anyString())).thenReturn(GolangDependencySet.empty())
        when(project.getRootDir()).thenReturn(resource)


        LocalDirectoryDependency local1 = localDependency('this/is/local1')

        VendorResolvedDependency vendor = vendorDependency(local1, 'this/is/vendor')
        GitNotationDependency vcsDependency = vcsDependency('this/is/vcs')
        LocalDirectoryDependency local2 = localDependency('this/is/local2')

        LRUMap map = new LRUMap()
        map[local1] = DependencyUtils.asGolangDependencySet(vcsDependency, local2, vendor)

        IOUtils.serialize(map, new File(resource, ".gogradle/cache/${PersistenceResolvedToDependenciesCache.simpleName}.bin"))


        when(resolver.produce('this/is/local1')).thenReturn(of(LocalDirectoryGolangPackage.of('this/is/local1', 'this/is/local1', toUnixString(resource))))
        when(resolver.produce('this/is/local2')).thenReturn(of(LocalDirectoryGolangPackage.of('this/is/local2', 'this/is/local2', toUnixString(resource))))
        when(resolver.produce('this/is/vcs')).thenReturn(of(VcsGolangPackage.builder()
                .withPath('this/is/vcs')
                .withRootPath('this/is/vcs')
                .withOriginalVcsInfo(VcsType.GIT, ['url'])
                .build()))
        when(resolver.produce('this/is/vendor')).thenReturn(of(UnrecognizedGolangPackage.of('this/is/vendor')))


        cache = new PersistenceResolvedToDependenciesCache(project, resolver)
    }

    @Test
    void 'caches should be reserved if path resolution does not change'() {
        cache.load()
        assert ReflectionUtils.getField(cache, 'container').size() == 1
    }

    @Test
    void 'cache should be discarded if vcs url changed'() {
        when(resolver.produce('this/is/vcs')).thenReturn(of(VcsGolangPackage.builder()
                .withPath('this/is/vcs')
                .withRootPath('this/is/vcs')
                .withOriginalVcsInfo(VcsType.GIT, ['anotherurl'])
                .build()))

        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    @Test
    void 'cache should be discarded if vcs path changed'() {
        when(resolver.produce('this/is/vcs')).thenReturn(of(VcsGolangPackage.builder()
                .withPath('this/is/vcs')
                .withRootPath('this/is')
                .withOriginalVcsInfo(VcsType.GIT, ['url'])
                .build()))

        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    @Test
    void 'cache should be discarded if unrecognized path changed'() {
        when(resolver.produce('this/is/vendor')).thenReturn(of(VcsGolangPackage.builder()
                .withPath('this/is/vendor')
                .withRootPath('this/is/vendor')
                .withOriginalVcsInfo(VcsType.GIT, ['url'])
                .build()))

        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    @Test
    void 'cache should be discarded if dir path changed'() {
        when(resolver.produce('this/is/local2')).thenReturn(of(UnrecognizedGolangPackage.of('this/is/local2')))

        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    VendorResolvedDependency vendorDependency(ResolvedDependency hostDependency, String path) {
        VendorResolvedDependency ret = VendorResolvedDependency.fromParent(path, hostDependency, new File(resource, path))
        ret.package = UnrecognizedGolangPackage.of(path)
        return ret
    }

    LocalDirectoryDependency localDependency(String name) {
        LocalDirectoryDependency ret = LocalDirectoryDependency.fromLocal(name, resource)
        ret.package = LocalDirectoryGolangPackage.of(name, name, toUnixString(resource))
        return ret
    }

    GitNotationDependency vcsDependency(String name) {
        GitNotationDependency ret = new GitNotationDependency()
        ret.name = name
        ret.url = 'url'
        ret.commit = 'commit'
        ret.package = VcsGolangPackage.builder()
                .withPath(name)
                .withRootPath(name)
                .withOriginalVcsInfo(VcsType.GIT, ['url'])
                .build()
        return ret
    }


}
