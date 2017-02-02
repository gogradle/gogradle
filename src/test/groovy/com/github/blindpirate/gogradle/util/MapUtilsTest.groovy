package com.github.blindpirate.gogradle.util

import org.junit.Test

class MapUtilsTest {
    @Test
    void 'asMapWithoutNull should succeed'() {
        assert MapUtils.asMapWithoutNull(null, '').isEmpty()
        assert MapUtils.asMapWithoutNull('', null).isEmpty()
    }
}
