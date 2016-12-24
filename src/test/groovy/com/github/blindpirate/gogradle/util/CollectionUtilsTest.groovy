package com.github.blindpirate.gogradle.util

import org.junit.Test

import static java.util.Optional.*

class CollectionUtilsTest {
    @Test
    void 'building immutable list should success'() {
        assert CollectionUtils.immutableList(1, 2, 3) == [1, 2, 3]
    }

    @Test(expected = Exception)
    void 'modifying an immutatble list should result in an exception'() {
        CollectionUtils.immutableList(1)[0] = 2
    }

    @Test
    void 'optional collect should success'() {
        CollectionUtils.collectOptional(empty(), of(1), empty(), of(2)) == [1, 2]
    }

    @Test
    void 'flattening a list should success'() {
        assert [1, 2, 3, 4, 5] == CollectionUtils.flatten([1, [2, 3], [[4]], [], 5])
    }

}
