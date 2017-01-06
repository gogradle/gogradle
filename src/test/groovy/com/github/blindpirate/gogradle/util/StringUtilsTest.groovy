package com.github.blindpirate.gogradle.util

import org.junit.Test

class StringUtilsTest {
    @Test
    void 'splitting and trimming should succeed'() {
        assertEquals(' a b c \t\nd  ', ' ', ['a', 'b', 'c', 'd'])
        assertEquals(' a b c \t\nd  ', '', ['a', 'b', 'c', 'd'])
        assertEquals(' a = b= c \t=\nd  ', '=', ['a', 'b', 'c', 'd'])
        assertEquals(' a sss bsss c \tsss\nd  ', 'sss', ['a', 'b', 'c', 'd'])
        assertEquals(' a - b- c \t-\nd  ', /\-/, ['a', 'b', 'c', 'd'])
        assertEquals(' a b c \t\nd  ', /\n/, ['a b c', 'd'])
        assertEquals(' a b c \t\nd  ', /\s/, ['a', 'b', 'c', 'd'])
    }

    private void assertEquals(String source, String regex, List result) {
        assert StringUtils.splitAndTrim(source, regex) == result as String[]
    }
}
