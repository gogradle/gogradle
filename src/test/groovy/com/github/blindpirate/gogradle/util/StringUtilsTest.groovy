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

import static com.github.blindpirate.gogradle.util.ExceptionHandler.UncheckedException

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
                '''${a}
${writer ->
list.each {
    def template ='${it.name}${it.url}'
    def result = ['${it.name}':it.name,'${it.url}':it.url].inject(template, {s,entry -> 
        s.replace(entry.key,entry.value)
        })
        
    writer << result    
}
}
''', [a: '1', list: [[name: 'name1', url: 'url1'], [name: 'name2', url: 'url2']]]) == '1\nname1url1name2url2\n'
    }

    @Test(expected = UncheckedException)
    void 'exception should be thrown if rendering fails'() {
        StringUtils.render('${Class.forName(/unexistent/)}', [:])
    }
}
