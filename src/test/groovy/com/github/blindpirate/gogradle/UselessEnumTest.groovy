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

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstallFileFilter
import com.github.blindpirate.gogradle.core.mode.BuildMode
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import org.junit.Test

import static com.github.blindpirate.gogradle.util.ReflectionUtils.callStaticMethod

class UselessEnumTest {
    @Test
    void 'values() and valueOf of all enums should succeed'() {
        [BuildMode,
         Os,
         Arch,
         CacheScope,
         AbstractNotationDependency.NoTransitivePredicate,
         DependencyInstallFileFilter,
         GogradleGlobal].each(this.&doUselessTest)
    }

    void doUselessTest(Class<? extends Enum> enumClass) {
        callStaticMethod(enumClass, 'values').each {
            Object value = callStaticMethod(enumClass, 'valueOf', it.name())
            assert it.is(value)
        }
    }
}
