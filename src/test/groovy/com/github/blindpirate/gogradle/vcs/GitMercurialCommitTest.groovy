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

class GitMercurialCommitTest {
    @Test
    void 'checking sem version should succeed'() {
        assert GitMercurialCommit.of('id', '1.2.3', 0L).satisfies('v1.2.3')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.2.3')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1.x')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.x')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1.X')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.X')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1.*')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.*')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('v1')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1')

        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('~1.2')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('1.0-1.3')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('!(2.x)')
        assert GitMercurialCommit.of('id', 'v1.2.3', 0L).satisfies('~2.0 | (1.4.* & !=1.4.5) | ~1.2')
    }
}
