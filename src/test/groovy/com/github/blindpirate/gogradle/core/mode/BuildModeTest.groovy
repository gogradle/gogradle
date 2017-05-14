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

package com.github.blindpirate.gogradle.core.mode

import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class BuildModeTest {
    GolangDependencySet declared = GolangDependencySet.empty()
    GolangDependencySet locked = GolangDependencySet.empty()
    GolangDependencySet vendor = GolangDependencySet.empty()

    GogradleRootProject gogradleRootProject = Mockito.mock(GogradleRootProject)

    VendorResolvedDependency a = new VendorResolvedDependencyForTest('a', 'a', 1L, gogradleRootProject, 'vendor/a')
    VendorResolvedDependency b = new VendorResolvedDependencyForTest('b', 'b', 1L, gogradleRootProject, 'vendor/a/vendor/b')
    VendorResolvedDependency cInVendor = new VendorResolvedDependencyForTest('c', 'c', 1L, gogradleRootProject, 'vendor/a/vendor/b/vendor/c')
    NotationDependency cInBuildDotGradle = DependencyUtils.mockWithName(NotationDependency, 'c')
    NotationDependency lockedC = DependencyUtils.mockWithName(NotationDependency, 'c')

    @Before
    void setUp() {
        a.dependencies.add(b)
        b.dependencies.add(cInVendor)

        vendor.add(a)
        declared.add(cInBuildDotGradle)
        locked.add(lockedC)
    }

    @Test
    void 'declared > locked > vendor in DEVELOP mode'() {
        // when
        GolangDependencySet result = BuildMode.DEVELOP.determine(declared, vendor, locked)
        // then
        assert result.size() == 2
        assert result.any { it.is(a) }
        assert result.any { it.is(cInBuildDotGradle) }
        assert a.dependencies.contains(b)
        assert b.dependencies.empty
    }

    @Test
    void 'vendor > locked > declared in REPRODUCIBLE mode'() {
        // when
        GolangDependencySet result = BuildMode.REPRODUCIBLE.determine(declared, vendor, locked)
        // then
        assert result.size() == 1
        assert result.any { it.is(a) }
        assert a.dependencies.contains(b)
        assert b.dependencies.any { it.is(cInVendor) }
    }
}
