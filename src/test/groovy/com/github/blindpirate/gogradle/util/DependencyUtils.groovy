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

package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class DependencyUtils {
    static GolangDependencySet asGolangDependencySet(GolangDependency... dependencies) {
        return dependencies.inject(new GolangDependencySet(), { ret, dependency ->
            ret.add(dependency)
            return ret
        })
    }

    static GolangDependency mockDependency(String name) {
        GolangDependency ret = mock(GolangDependency)
        when(ret.getName()).thenReturn(name)
        return ret
    }

    static mockWithName(Class clazz, String name) {
        def ret = mock(clazz)
        when(ret.getName()).thenReturn(name)
        return ret
    }

    static ResolvedDependency mockResolvedDependency(String name) {
        ResolvedDependency ret = mock(ResolvedDependency)
        when(ret.getName()).thenReturn(name)
        when(ret.formatVersion()).thenReturn('version')
        when(ret.toString()).thenReturn(name)
        when(ret.resolve()).thenReturn(ret)
        return ret
    }

    static VendorResolvedDependency mockVendorResolvedDependency(String name, ResolvedDependency host, String vendorPath) {
        VendorResolvedDependency ret = mock(VendorResolvedDependency)
        when(ret.getName()).thenReturn(name)
        when(ret.getHostDependency()).thenReturn(host)
        when(ret.getRelativePathToHost()).thenReturn(vendorPath)
        return ret
    }

    static Set getExclusionSpecs(AbstractNotationDependency target) {
        return ReflectionUtils.getField(target, 'transitiveDepExclusions')
    }

}
