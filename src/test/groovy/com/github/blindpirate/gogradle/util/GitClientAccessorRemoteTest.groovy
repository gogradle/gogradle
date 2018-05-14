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
import com.github.blindpirate.gogradle.support.GitServer
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.vcs.git.GitClientAccessor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.support.GitServer.*

@RunWith(GogradleRunner)
@OnlyWhen(value = '"git version".execute()', ignoreTestWhenException = OnlyWhen.ExceptionStrategy.TRUE)
@WithMockInjector
@WithResource
class GitClientAccessorRemoteTest {
    GitServer gitServer = new GitServer()
    File resource
    File repo
    File clone
    GitClientAccessor accessor
    String initialCommit

    @Before
    void setup() {
        accessor = new GitClientAccessor(new ProcessUtils())

        repo = IOUtils.mkdir(resource, 'repo')
        clone = IOUtils.mkdir(resource, 'clone')

        initialCommit = createRepository(repo, 'commit1.go')
        gitServer.addRepo('a', repo)
        gitServer.start(DEFAULT_PORT)
    }

    @After
    void cleanup() {
        gitServer.stop()
    }

    @Test
    void 'getting head commit of remote master should succeed after git fetch'() {
        accessor.clone("http://localhost:${DEFAULT_PORT}/a", clone)
        String commit2 = addFileToRepository(repo, 'commit2.go')

        accessor.update(clone)
        assert accessor.headCommitOfBranch(clone, accessor.getDefaultBranch(clone)).id.startsWith(commit2)

        accessor.checkout(clone, commit2)
        assert new File(clone, 'commit2.go').exists()
    }

    @Test
    void 'getting head commit of remote master should succeed if remote repo is force updated'() {
        accessor.clone("http://localhost:${DEFAULT_PORT}/a", clone)
        IOUtils.forceDelete(new File(repo, 'commit1.go'))
        IOUtils.write(repo, 'commit2.go', '')
        git('add .', repo)
        git('commit --amend -m amend', repo)

        accessor.update(clone)
        String head = accessor.headCommitOfBranch(clone, accessor.getDefaultBranch(clone)).id
        accessor.checkout(clone, head)

        assert !new File(clone, 'commit1.go').exists()
        assert new File(clone, 'commit2.go').exists()
    }

    @Test
    void 'getting head commit of remote master should succeed when local is detached HEAD'() {
        accessor.clone("http://localhost:${DEFAULT_PORT}/a", clone)
        String commit2 = addFileToRepository(repo, 'commit2.go')

        accessor.checkout(clone, initialCommit)

        accessor.update(clone)
        assert accessor.headCommitOfBranch(clone, accessor.getDefaultBranch(clone)).id.startsWith(commit2)

        accessor.checkout(clone, commit2)
        assert new File(clone, 'commit2.go').exists()
    }

    @Test
    void 'getting non-master default branch of remote should succeed'() {
        newBranch(repo, 'default')
        accessor.clone("http://localhost:${DEFAULT_PORT}/a", clone)

        assert accessor.getDefaultBranch(clone) == 'default'
    }

    @Test
    void 'getting remote tag should succeed'() {
        accessor.clone("http://localhost:${DEFAULT_PORT}/a", clone)
        git('tag v1.0', repo)

        assert !accessor.findCommitByTagOrBranch(clone, 'v1.0').isPresent()

        accessor.update(clone)

        assert accessor.findCommitByTagOrBranch(clone, 'v1.0').get().id.startsWith(initialCommit)
    }
}
