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
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultNotationParserTest {

    @Mock
    MapNotationParser mapNotationParser
    @Mock
    NotationConverter notationConverter
    @Mock
    GolangDependency dependency

    DefaultNotationParser parser

    @Before
    void setUp() {
        parser = new DefaultNotationParser(mapNotationParser, notationConverter)
    }

    @Test(expected = DependencyResolutionException)
    void 'parsing unrecognized class should fail'() {
        parser.parse({})
    }

    @Test
    void 'string should be converted to map before parsing'() {
        // given
        String stringNotation = 'notation'
        Map mapNotation = [name: 'notation']
        when(notationConverter.convert(stringNotation)).thenReturn(mapNotation)
        // when
        parser.parse(stringNotation)

        // then
        verify(mapNotationParser).parse(mapNotation)
    }

    @Test
    void 'map parsing should be delegated to MapNotationParser'() {
        // given
        Map notation = [name: 'notation']

        // when
        parser.parse(notation)

        // then
        verify(mapNotationParser).parse(notation)
    }

}
