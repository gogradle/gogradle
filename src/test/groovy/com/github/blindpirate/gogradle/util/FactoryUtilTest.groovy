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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.common.Factory
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class FactoryUtilTest {
    @Mock
    Factory factory1
    @Mock
    Factory factory2
    @Mock
    Object material
    @Mock
    Object product

    @Test
    void 'production with PickyFactory should succeed'() {
        // given:
        when(factory1.produce(material)).thenReturn(Optional.empty())
        when(factory2.produce(material)).thenReturn(Optional.of(product))

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then
        assert result.get() == product
    }

    @Test
    void 'production when not accepted should fail'() {
        // given:
        when(factory1.produce(material)).thenReturn(Optional.empty())
        when(factory2.produce(material)).thenReturn(Optional.empty())

        // when:
        def result = FactoryUtil.produce([factory1, factory2], material)

        // then:
        assert !result.isPresent()
    }
}
