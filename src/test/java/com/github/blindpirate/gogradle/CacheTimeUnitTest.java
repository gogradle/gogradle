/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.blindpirate.gogradle;

import javax.annotation.Nonnull;

import org.junit.Test;

import static com.github.blindpirate.gogradle.CacheTimeUnit.DAYS;
import static com.github.blindpirate.gogradle.CacheTimeUnit.HOURS;
import static com.github.blindpirate.gogradle.CacheTimeUnit.MINUTES;
import static com.github.blindpirate.gogradle.CacheTimeUnit.SECONDS;
import static com.github.blindpirate.gogradle.CacheTimeUnit.fromString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class CacheTimeUnitTest {

    @Test
    public void testToSeconds() {
        assertEquals(1, SECONDS.toSeconds(1));
        assertEquals(60, MINUTES.toSeconds(1));
        assertEquals(3600, HOURS.toSeconds(1));
        assertEquals(86400, DAYS.toSeconds(1));
    }

    @Test
    public void testFromString() {
        vetTimeUnit(SECONDS, "seconds");
        vetTimeUnit(MINUTES, "minutes");
        vetTimeUnit(HOURS, "hours");
        vetTimeUnit(DAYS, "days");
    }

    private void vetTimeUnit(@Nonnull CacheTimeUnit timeUnit, @Nonnull String s) {
        assertEquals(timeUnit, fromString(s));
        assertEquals(timeUnit, fromString(s.substring(0, s.length() - 1)));
        try {
            fromString(s.substring(0, s.length() - 2));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
