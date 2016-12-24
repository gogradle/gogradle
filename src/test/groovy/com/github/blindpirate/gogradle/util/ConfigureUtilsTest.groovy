package com.github.blindpirate.gogradle.util

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.Test

class ConfigureUtilsTest {

    class Bean {
        String a
        Integer b
        Object c
    }

    Bean bean = new Bean()

    @Test
    void 'setting an value dynamically should success'() {
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

    @Test(expected = GroovyCastException)
    void 'setting property with incompatible type should result in an exception'() {
        ConfigureUtils.configureByMapQuietly([a: 1, b: ''], bean)
    }
}
