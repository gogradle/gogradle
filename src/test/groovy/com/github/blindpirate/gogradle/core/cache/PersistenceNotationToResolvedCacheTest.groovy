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
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockWithName
import static com.github.blindpirate.gogradle.util.MockUtils.mockVcsPackage
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class PersistenceNotationToResolvedCacheTest {
    @Mock
    Project project
    @Mock
    PackagePathResolver packagePathResolver

    File rootDir = new File("/unexistent")

    PersistenceNotationToResolvedCache cache

    @Before
    void setUp() {
        when(project.getRootDir()).thenReturn(rootDir)
        cache = new PersistenceNotationToResolvedCache(project, packagePathResolver)
    }

    @Test
    void 'dependencies whose package changes should be removed'() {
        NotationDependency notation1 = mockWithName(NotationDependency, 'notation1')
        NotationDependency notation2 = mockWithName(NotationDependency, 'notation2')
        ResolvedDependency resolved1 = mockWithName(ResolvedDependency, 'resolved1')
        ResolvedDependency resolved2 = mockWithName(ResolvedDependency, 'resolved2')

        NotationDependency transitive = mockWithName(NotationDependency, 'github.com/user/package')
        when(transitive.getPackage()).thenReturn(UnrecognizedGolangPackage.of('github.com/user/package'))

        GolangDependencySet set1 = asGolangDependencySet(transitive)
        when(resolved1.getDependencies()).thenReturn(set1)
        when(resolved2.getDependencies()).thenReturn(GolangDependencySet.empty())

        Map container = ReflectionUtils.getField(cache, 'container')
        container.put(notation1, resolved1)
        container.put(notation2, resolved2)

        when(packagePathResolver.produce('github.com/user/package')).thenReturn(Optional.of(mockVcsPackage()))

        cache.load()

        assert !container.containsKey(notation1)
        assert container.containsKey(notation2)
    }

}
