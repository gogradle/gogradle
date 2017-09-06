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

import org.junit.Test;

import static com.github.blindpirate.gogradle.core.mode.BuildMode.DEVELOP;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.REPRODUCIBLE;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.fromString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class BuildModeTest {

    @Test
    public void testFromString() {
        assertEquals(DEVELOP, fromString("DEVELOP"));
        assertEquals(DEVELOP, fromString("DEV"));
        assertEquals(REPRODUCIBLE, fromString("REPRODUCIBLE"));
        assertEquals(REPRODUCIBLE, fromString("REP"));

        try {
            fromString("DEVE");
            fail("DEVE is invalid");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            fromString("REPR");
            fail("REPR is invalid");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            fromString("FOO");
            fail("FOO is invalid");
        } catch (IllegalArgumentException ignored) {
        }
    }
}
