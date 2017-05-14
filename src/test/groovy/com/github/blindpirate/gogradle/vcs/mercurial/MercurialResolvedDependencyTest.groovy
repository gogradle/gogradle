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

package com.github.blindpirate.gogradle.vcs.mercurial

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.vcs.Mercurial
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(GogradleRunner)
class MercurialResolvedDependencyTest {
    @Test
    @WithMockInjector
    void 'correct installer class should be returned'() {
        DependencyManager installer = Mockito.mock(DependencyManager)
        Mockito.when(GogradleGlobal.INSTANCE.getInjector().getInstance(Key.get(DependencyManager, Mercurial))).thenReturn(installer)
        assert new MercurialResolvedDependency(null, null, null, 0L).installer == installer
    }

    @Test
    void 'correct vcs type should be returned'() {
        assert new MercurialResolvedDependency(null, null, null, 0L).vcsType == VcsType.MERCURIAL
    }
}
