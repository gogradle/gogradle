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

package com.github.blindpirate.gogradle.core.dependency.install

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import org.junit.Before
import org.junit.Test

import static com.github.blindpirate.gogradle.util.DependencyUtils.mockWithName
import static org.mockito.Mockito.when

class VendorSupportMixinTest {
    ResolvedDependency resolvedDependency = mockWithName(ResolvedDependency, 'resolved')
    ResolvedDependency hostDependency = mockWithName(ResolvedDependency, 'host')
    VendorResolvedDependency vendorResolvedDependency = mockWithName(VendorResolvedDependency, 'vendor')

    VendorSupportMixin mixin = new VendorSupportMixin() {
    }

    @Before
    void setUp() {
        when(vendorResolvedDependency.getHostDependency()).thenReturn(hostDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn('vendor/github.com/a/b')
    }

    @Test
    void 'determining real path should succeed'() {
        assert mixin.determineDependency(resolvedDependency).is(resolvedDependency)
        assert mixin.determineDependency(vendorResolvedDependency).is(hostDependency)
    }

    @Test
    void 'determining relative path to host should succeed'() {
        assert mixin.determineRelativePath(vendorResolvedDependency) == 'vendor/github.com/a/b'
        assert mixin.determineRelativePath(resolvedDependency) == '.'
    }
}
