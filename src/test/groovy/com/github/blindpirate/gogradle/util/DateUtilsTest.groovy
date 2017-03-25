package com.github.blindpirate.gogradle.util

import org.junit.Test

class DateUtilsTest {
    @Test
    void 'getting ms from second should succeed'() {
        assert DateUtils.toMilliseconds(1) == 1000
    }

    @Test
    void 'getting ms from double second should succeed'() {
        assert DateUtils.toMilliseconds(1d) == 1000L
    }

    @Test
    void 'formatting current time should succeed'() {
        String now = DateUtils.formatNow('yyyyMMddHHmmss')
        Date date = Date.parse('yyyyMMddHHmmss', now)
        assert new Date().getTime() - date.getTime() < 1000
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if time is invalid'() {
        DateUtils.parseRaw('1481274259')
    }
}
