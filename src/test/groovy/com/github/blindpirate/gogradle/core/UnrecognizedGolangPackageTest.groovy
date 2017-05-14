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

package com.github.blindpirate.gogradle.core

import org.junit.Test

import static com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage.of

class UnrecognizedGolangPackageTest {
    UnrecognizedGolangPackage unrecognizedGolangPackage = of('golang/x')

    @Test
    void 'path longer than unrecognized package should be empty'() {
        assert !unrecognizedGolangPackage.resolve('golang/x/tools').isPresent()
    }

    @Test
    void 'path shorter than unrecognized package should also be unrecognized'() {
        assert unrecognizedGolangPackage.resolve('golang').get() instanceof UnrecognizedGolangPackage
    }

    @Test
    void 'toString() should succeed'() {
        assert unrecognizedGolangPackage.toString() == "UnrecognizedGolangPackage{path='golang/x'}"
    }

    @Test
    void 'equality check should succeed'() {
        assert unrecognizedGolangPackage == unrecognizedGolangPackage
        assert unrecognizedGolangPackage != null
        assert unrecognizedGolangPackage != StandardGolangPackage.of("golang/x")
        assert unrecognizedGolangPackage == of('golang/x')
    }
}
