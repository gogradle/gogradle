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
import com.github.blindpirate.gogradle.core.exceptions.BuildException
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithGitRepos
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.vcs.GitMercurialCommit
import com.github.blindpirate.gogradle.vcs.git.GitClientAccessor
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Paths

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@OnlyWhen(value = '"git version".execute()', ignoreTestWhenException = OnlyWhen.ExceptionStrategy.TRUE)
@WithMockInjector
class GitClientAccessorTest {

    File resource //= new File('/Users/zhb/Projects/gogradle/src/test/resources/simple-repo')

    File repositories

    GitClientAccessor accessor

    private static final String INITIAL_COMMIT = 'c13e73f962af659ddbc8cd593d5b002235da3fb3'

    @Before
    void setUp() {
        accessor = new GitClientAccessor(new ProcessUtils())
    }

    @Test(expected = IllegalStateException)
    void 'client should be considered as inexistent if error occurs'() {
        ProcessUtils processUtils = mock(ProcessUtils)
        accessor = new GitClientAccessor(processUtils)
        when(processUtils.runAndGetStdout(['git', 'version'] as String[])).thenThrow(IOException)
        accessor.ensureClientExists()
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'getting head commit of master branch should succeed'() {
        assert accessor.headCommitOfBranch(resource, 'master').id.length() == 40
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'getting head commit of master branch after checkout should succeed'() {
        String head = accessor.headCommitOfBranch(resource, 'master').id
        accessor.checkout(resource, INITIAL_COMMIT)
        assert head == accessor.headCommitOfBranch(resource, 'master').id
    }

    @Test
    @WithGitRepos('test-git-repos.zip')
    void 'getting default branch should succeed'() {
        File repoWithMaster = new File(repositories, 'simple-repo')
        File repoWithoutMaster = new File(repositories, 'repo-without-master')

        assert accessor.getDefaultBranch(repoWithMaster) == 'master'
        assert accessor.getDefaultBranch(repoWithoutMaster) == 'mybranch'
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'getting remote url of repository should succeed'() {
        assert accessor.getRemoteUrl(resource) == 'http://localhost:8080/simple-repo'
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'finding initial commit should succeed'() {
        assert accessor.findCommit(resource, INITIAL_COMMIT[0..5]).get().id == INITIAL_COMMIT
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'finding inexistent commit should fail'() {
        assert !accessor.findCommit(resource, 'nonexistence').isPresent()
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'getting a tag should succeed'() {
        assert accessor.findCommitByTagOrBranch(resource, '1.0.0').get().id == '8f593bdb1d3fbd799a85d6ccf7b38373847ee045'
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'getting a branch should succeed'() {
        assert accessor.findCommitByTagOrBranch(resource, 'master').get().id == 'd5a49603787eb132fad5bbdcaaca764ba0982137'
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'getting an inexistent tag should fail'() {
        assert !accessor.findCommitByTagOrBranch(resource, 'nonexistence').isPresent()
    }

    @Test
    @WithResource('')
    void 'empty list should be returned if there is no tag'() {
        FileRepositoryBuilder.create(new File(resource, '.git')).create()
        assert accessor.getAllTags(resource) == []
    }

    @Test
    @WithResource('')
    @WithGitRepos('test-git-repos.zip')
    void 'cloning with https and submodules should succeed'() {
        // when
        accessor.clone("http://localhost:8080/simple-repo", resource)
        // then
        assert new File(resource, '.git').exists()
        assert new File(resource, 'vendor/submodule.go').exists()
        assert accessor.headCommitOfBranch(resource, 'master').id.length() == 40
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'reset to initial commit should succeed'() {
        assert new File(resource, 'helloworld.go').exists()
        accessor.checkout(resource, INITIAL_COMMIT)
        assert !new File(resource, 'helloworld.go').exists()
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'finding tags should succeed'() {
        List<GitMercurialCommit> commits = accessor.getAllTags(resource)
        assert commits.collect {
            it.tag
        } == ['3.0.0', '2.1.2', '2.1.1', '2.1.0', '2.0', '1.2.0', '1.0.0', '0.0.3-prerelease', 'v0.0.2', '0.0.1']
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    @WithGitRepos('test-git-repos.zip')
    void 'git update with should succeed'() {
        accessor.checkout(resource, INITIAL_COMMIT)
        accessor.checkout(resource, accessor.getDefaultBranch(resource))
        accessor.update(resource)
        assert new File(resource, 'vendor/submodule.go').exists()
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'getting commit time of path should succeed'() {
        // Sat Apr 1 12:18:52 2017 +0800
        assert accessor.lastCommitTimeOfPath(resource, Paths.get('helloworld.go')) == 1491020332000L
    }

    @Test
    @WithResource('out-of-date-repo.zip')
    void 'commit time should be the nearest time to current repo snapshot'() {
        long t0 = accessor.lastCommitTimeOfPath(resource, Paths.get('README.md'))
        accessor.checkout(resource, INITIAL_COMMIT)
        long t1 = accessor.lastCommitTimeOfPath(resource, Paths.get('README.md'))
        assert t0 > t1
    }

    @Test(expected = BuildException)
    @WithResource('out-of-date-repo.zip')
    void 'getting path at a commit when it does not exist should throw an exception'() {
        // helloworld.go didn't exist in initial commit
        accessor.checkout(resource, INITIAL_COMMIT)
        accessor.lastCommitTimeOfPath(resource, Paths.get('helloworld.go'))
    }
}
