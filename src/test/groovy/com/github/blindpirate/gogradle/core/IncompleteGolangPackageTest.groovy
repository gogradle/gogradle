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

import static com.github.blindpirate.gogradle.core.IncompleteGolangPackage.of

class IncompleteGolangPackageTest {
    @Test
    void 'empty result should be returned when an incomplete package resolves a longer path'() {
        assert !of('incomplete/a').resolve('incomplete/a/b').isPresent()
        assert !of('incomplete/a').resolve('incomplete/a/b/c').isPresent()
    }

    @Test
    void 'shorter path of an incomplete path should also be incomplete'() {
        assert of('incomplete/a').resolve('incomplete').get() instanceof IncompleteGolangPackage
    }

    @Test
    void 'toString should succeed'(){
       assert of('incomplete/a').toString()=="IncompleteGolangPackage{path='incomplete/a'}"
    }
}
