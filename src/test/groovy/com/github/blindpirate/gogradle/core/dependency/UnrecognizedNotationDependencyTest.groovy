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

import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.exceptions.UnrecognizedPackageException
import org.junit.Test

class UnrecognizedNotationDependencyTest {

    UnrecognizedGolangPackage pkg = UnrecognizedGolangPackage.of('unrecognized')

    UnrecognizedNotationDependency dependency = UnrecognizedNotationDependency.of(pkg)

    @Test(expected = UnsupportedOperationException)
    void 'isFirstLevel is not supported'() {
        dependency.isFirstLevel()
    }

    @Test(expected = UnsupportedOperationException)
    void 'isConcrete is not supported'() {
        dependency.getCacheScope()
    }

    @Test(expected = UnrecognizedPackageException)
    void 'getTransitiveDepExclusions is not supported'() {
        dependency.getTransitiveDepExclusions()
    }

    @Test(expected = UnrecognizedPackageException)
    void 'resolve is not supported'() {
        dependency.resolve(null)
    }

    @Test
    void 'getting package should succeed'() {
        assert dependency.package.is(pkg)
    }
}
