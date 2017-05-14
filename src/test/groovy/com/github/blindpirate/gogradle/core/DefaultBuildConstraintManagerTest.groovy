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

package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.crossplatform.Os
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultBuildConstraintManagerTest {
    DefaultBuildConstraintManager manager

    @Mock
    GolangPluginSetting setting
    @Mock
    GoBinaryManager goBinaryManager

    @Before
    void setUp() {
        manager = new DefaultBuildConstraintManager(goBinaryManager, setting)
    }

    @Test
    void 'predefined constraints should be added'() {
        // given
        when(goBinaryManager.getGoVersion()).thenReturn("1.1")
        // when
        manager.prepareConstraints()
        // then
        assert manager.getAllConstraints().contains(Os.getHostOs().toString())
        assert manager.getAllConstraints().contains(Arch.getHostArch().toString())
        assert manager.getAllConstraints().contains("go1.1")
        assert !manager.getAllConstraints().contains("go1.2")
        assert !manager.getAllConstraints().contains("go1.0")
    }

    @Test
    void 'all old versions should be added'() {
        // given
        when(goBinaryManager.getGoVersion()).thenReturn("1.100")
        // when
        manager.prepareConstraints()
        // then
        (1..100).each { assert manager.getAllConstraints().contains("go1." + it) }
    }

    @Test(expected = IllegalStateException)
    void 'only go1 is supported'() {
        when(goBinaryManager.getGoVersion()).thenReturn('2.0.0')
        manager.prepareConstraints()
    }
}
