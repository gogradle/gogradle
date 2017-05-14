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

package com.github.blindpirate.gogradle.core.dependency.parse

import org.junit.Test

class GitMercurialNotationConverterTest {

    GitMercurialNotationConverter converter = new GitMercurialNotationConverter()

    @Test
    void 'package with only name should be parsed correctly'() {
        assert converter.convert('github.com/a/b') == [name: 'github.com/a/b']
    }

    @Test
    void 'package with tag should be parsed correctly'() {
        assertWithTag('github.com/a/b@v1.0.0', 'v1.0.0')
        assertWithTag('github.com/a/b@v1.0.0-prerelease', 'v1.0.0-prerelease')
        assertWithTag('github.com/a/b@tag-with@', 'tag-with@')
        assertWithTag('github.com/a/b@tag-with#', 'tag-with#')
        assertWithTag('github.com/a/b@tag-contains@1', 'tag-contains@1')
        assertWithTag('github.com/a/b@tag-contains#1', 'tag-contains#1')
        assertWithTag('github.com/a/b@tag-contains@and#at-the-same-time', 'tag-contains@and#at-the-same-time')
    }

    @Test
    void 'package with commit should be parsed correctly'() {
        assert converter.convert('github.com/a/b#commitId') == [name: 'github.com/a/b', commit: 'commitId']
    }

    // https://github.com/zafarkhaja/jsemver
    @Test
    void 'package with sem version should be parsed correctly'() {
        assertWithTag('github.com/a/b@1.*', '1.*')
        assertWithTag('github.com/a/b@1.x', '1.x')
        assertWithTag('github.com/a/b@~1.5', '~1.5')
        assertWithTag('github.com/a/b@1.0-2.0', '1.0-2.0')
        assertWithTag('github.com/a/b@^0.2.3', '^0.2.3')
        assertWithTag('github.com/a/b@1', '1')
        assertWithTag('github.com/a/b@!(1.x)', '!(1.x)')
        assertWithTag('github.com/a/b@~1.3 | (1.4.* & !=1.4.5) | ~2', '~1.3 | (1.4.* & !=1.4.5) | ~2')
    }

    @Test(expected = IllegalStateException)
    void 'invalid notation should cause an exception'() {
        converter.convert('github.com/a/b#')
    }

    void assertWithTag(String notation, String tag) {
        assert converter.convert(notation) == [name: 'github.com/a/b', tag: tag]
    }
}
