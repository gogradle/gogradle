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

class ConfigureUtilsTest {

    class Bean {
        String a
        Integer b
        Object c
    }

    Bean bean = new Bean()

    @Test
    void 'setting an value dynamically should succeed'() {
        // when
        ConfigureUtils.configureByMapQuietly([a: 'a', b: 1, c: []], bean)
        // then
        assert bean.a == 'a'
        assert bean.b == 1
        assert bean.c == []
    }

    @Test
    void 'extra properties should be ignored quietly'() {
        // when
        ConfigureUtils.configureByMapQuietly([a: 'a', d: 1, e: []], bean)
        // then
        assert bean.a == 'a'
        assert bean.b == null
        assert bean.c == null
    }

    @Test
    void 'empty properties should be considered matched'() {
        assert ConfigureUtils.match([:], bean)
    }

    @Test
    void 'setting property with incompatible type should result in an exception'() {
        try {
            ConfigureUtils.configureByMapQuietly([a: 1, b: ''], bean)
            assert false
        } catch (Exception e) {
            assert ExceptionHandler.getRootCause(e) instanceof NoSuchMethodException
        }
    }
}
