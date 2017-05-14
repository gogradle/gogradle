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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(GogradleRunner)
class MercurialDependencyManagerTest {
    @Mock
    HgClientAccessor hgClientAccessor

    MercurialDependencyManager manager

    @Before
    void setUp() {
        manager = new MercurialDependencyManager(hgClientAccessor, null, null)
    }

    @Test
    void 'getting accessor should succeed'() {
        assert manager.getAccessor().is(hgClientAccessor)
    }

    @Test
    void 'vcs type should be mercurial'() {
        assert manager.getVcsType() == VcsType.MERCURIAL
    }
}
