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
