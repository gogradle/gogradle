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

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.MockRefreshDependencies
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.google.inject.Key
import org.junit.Test
import org.junit.runner.RunWith

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
@WithMockInjector
class GogradleGlobalTest {

    @Test
    void 'global injection should succeed'() {
        // given
        Key key = mock(Key)
        // when
        GogradleGlobal.getInstance(PackagePathResolver)
        GogradleGlobal.getInstance(key)
        // then
        verify(GogradleGlobal.INSTANCE.injector).getInstance(PackagePathResolver)
        verify(GogradleGlobal.INSTANCE.injector).getInstance(key)
    }

    @Test
    @MockRefreshDependencies(false)
    void 'jvm parameter _Dgogradle_refresh true should succeed'() {
        String originalValue = System.getProperty('gogradle.refresh')
        System.setProperty('gogradle.refresh', 'true')

        assert GogradleGlobal.isRefreshDependencies()

        System.setProperty('gogradle.refresh', originalValue ?: '')
    }
}
