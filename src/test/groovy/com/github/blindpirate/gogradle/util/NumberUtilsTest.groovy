package com.github.blindpirate.gogradle.util

import org.junit.Test

class NumberUtilsTest {
    @Test
    void 'calculating percentage should succeed'() {
        assert NumberUtils.percentage(1L, 100L) == 1
        assert NumberUtils.percentage(45.555d, 100d) == 46
        assert NumberUtils.percentage(99L, 100d) == 99
    }
}
