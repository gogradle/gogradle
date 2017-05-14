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
