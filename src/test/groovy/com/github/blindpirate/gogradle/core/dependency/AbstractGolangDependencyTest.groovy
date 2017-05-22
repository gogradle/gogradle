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

import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.util.MockUtils
import org.junit.Test
import org.mockito.Mockito

class AbstractGolangDependencyTest {
    AbstractGolangDependency dependency = Mockito.mock(AbstractGolangDependency, Mockito.CALLS_REAL_METHODS)

    @Test(expected = UnsupportedOperationException)
    void 'copy() should be forbidden'() {
        dependency.copy()
    }

    @Test(expected = UnsupportedOperationException)
    void 'getGroup() should be forbidden'() {
        dependency.getGroup()
    }

    @Test(expected = UnsupportedOperationException)
    void 'getVersion() should be forbidden'() {
        dependency.getVersion()
    }

    @Test(expected = UnsupportedOperationException)
    void 'contentEquals() should be forbidden'() {
        dependency.contentEquals(null)
    }

    @Test
    void 'cloning should succeed'() {
        // given
        AbstractGolangDependency dependency = new AbstractGolangDependencyForTest()
        dependency.name = 'name'
        dependency.package = MockUtils.mockVcsPackage()
        dependency.firstLevel = true

        // when
        AbstractGolangDependency clone = dependency.clone()
        // then
        assert clone.name == 'name'
        assert clone.firstLevel
        assert MockUtils.isMockVcsPackage(dependency.package)
    }

    @Test
    void 'setting subpackages should succeed'() {
        AbstractGolangDependency dependency = new AbstractGolangDependencyForTest()

        dependency.subpackages = ['a']
        assert dependency.subpackages == ['a'] as Set

        dependency.subpackages = 'b'
        assert dependency.subpackages == ['b'] as Set

        dependency.subpackage = ['c']
        assert dependency.subpackages == ['c'] as Set

        dependency.subpackage = 'd'
        assert dependency.subpackages == ['d'] as Set
    }

    @Test
    void 'subpackages should be included in equals'() {
        AbstractGolangDependency dependency1 = new AbstractGolangDependencyForTest()
        AbstractGolangDependency dependency2 = new AbstractGolangDependencyForTest()

        assert dependency1 == dependency2

        dependency1.subpackage = ['...', '.']
        assert dependency1 != dependency2
    }

    static class AbstractGolangDependencyForTest extends AbstractGolangDependency {
        private static final long serialVersionUID = 1L

        @Override
        ResolvedDependency resolve(ResolveContext context) {
            return null
        }

        @Override
        CacheScope getCacheScope() {
            return null
        }
    }
}
