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

package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import org.junit.Test

class AutoConfigureMapNotationParserTest {

    static class WithoutDefaultConstructor extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        private WithoutDefaultConstructor() {
        }

        @Override
        protected ResolvedDependency doResolve(ResolveContext context) {
            return null
        }

        @Override
        CacheScope getCacheScope() {
            return null
        }
    }

    static class WithDefaultConstructor extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        @Override
        protected ResolvedDependency doResolve(ResolveContext context) {
            return null
        }

        @Override
        CacheScope getCacheScope() {
            return null
        }
    }

    @Test(expected = IllegalStateException)
    void 'implementation class should provide a class with default constructor'() {
        new AutoConfigureMapNotationParser<WithoutDefaultConstructor>() {
        }.parse([:])
    }

    @Test
    void 'instance of WithDefaultConstructor'() {
        assert new AutoConfigureMapNotationParser<WithDefaultConstructor>() {
        }.parse([:]) instanceof WithDefaultConstructor
    }
}
