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

import org.junit.Test

import static java.util.Optional.empty
import static java.util.Optional.of

class CollectionUtilsTest {
    @Test
    void 'empty checking should succeed'() {
        assert CollectionUtils.isEmpty([])
        assert CollectionUtils.isEmpty(null)
    }

    @Test
    void 'building immutable list should succeed'() {
        assert CollectionUtils.immutableList(1, 2, 3) == [1, 2, 3]
    }

    @Test(expected = Exception)
    void 'modifying an immutatble list should result in an exception'() {
        CollectionUtils.immutableList(1)[0] = 2
    }

    @Test
    void 'optional collect should succeed'() {
        CollectionUtils.collectOptional(empty(), of(1), empty(), of(2)) == [1, 2]
    }

    @Test
    void 'flattening a list should succeed'() {
        assert [1, 2, 3, 4, 5] == CollectionUtils.flatten([[1], [2, 3], [], [4, 5]])
    }

    @Test
    void 'collect string from collections and arrays should succeed'() {
        assert CollectionUtils.asStringList('1', ['2', '3'], ['4', '5'] as String[]) == ['1', '2', '3', '4', '5']
    }

}
