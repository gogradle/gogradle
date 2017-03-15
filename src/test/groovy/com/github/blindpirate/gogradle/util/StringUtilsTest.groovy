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

    @Test
    void 'rendering simple template should succeed'() {
        assert StringUtils.render('${a}${b}${c}', [a: '1', b: '2', c: '3']) == '123'
    }

    @Test
    void 'rendering complicated template should succeed'() {
        assert StringUtils.render(
                '''
<%=a %>
<%
list.each {
    println it.name
}
%>
''', [a: '1', list: [[name: 'name1'], [name: 'name2']]]).replaceAll('\n', '') == '1name1name2'
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if rendering fails'() {
        StringUtils.render('${Class.forName(/unexistent/)}', [:])
    }
}
