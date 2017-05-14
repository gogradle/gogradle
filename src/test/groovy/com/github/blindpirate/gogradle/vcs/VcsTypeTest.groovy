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

package com.github.blindpirate.gogradle.vcs

import org.junit.Test

import static com.github.blindpirate.gogradle.vcs.VcsType.of

class VcsTypeTest {

    @Test
    void 'get vcs type by name should succeed'() {
        assert of('git').get() == VcsType.GIT
        assert of('hg').get() == VcsType.MERCURIAL
        assert of('svn').get() == VcsType.SVN
        assert of('bzr').get() == VcsType.BAZAAR

        assert !of('a').isPresent()
    }

    @Test
    void 'test valueOf'() {
        assert VcsType.valueOf('GIT') == VcsType.GIT
    }

}
