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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VendorMapNotationParserTest {
    @Mock
    MapNotationParser mapNotationParser
    @Mock
    NotationDependency hostDependency

    VendorMapNotationParser parser

    @Before
    void setUp() {
        parser = new VendorMapNotationParser(mapNotationParser)
    }

    @Test
    void 'parsing vendor notation dependency should succeed'() {
        // given
        when(mapNotationParser.parse([name: 'github.com/c/d'])).thenReturn(hostDependency)
        // when
        VendorNotationDependency result = parser.parse([name: 'github.com/a/b', host: [name: 'github.com/c/d'], vendorPath: 'vendor/github.com/a/b'])
        // then
        assert result.hostNotationDependency.is(hostDependency)
        assert result.vendorPath == 'vendor/github.com/a/b'
    }
}
