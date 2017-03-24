package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.exceptions.BuildException
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.vcs.GitMercurialCommit
import com.github.blindpirate.gogradle.vcs.git.GitClientAccessor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource("test-for-gogradle.zip")
@WithMockInjector
@OnlyWhen(value = '"git version".execute()', ignoreTestWhenException = OnlyWhen.ExceptionStrategy.TRUE)
class GitClientAccessorTest {

//    format
//    blindpirate committed on Dec 4, 2016
//    a8d7650
//    ----------
//    3.0.0
//    blindpirate committed on Dec 4, 2016
//    4a06b73
//    ----------
//    2.1.2
//    blindpirate committed on Dec 4, 2016
//    06325a9
//    ----------
//    2.1.1
//    blindpirate committed on Dec 4, 2016
//    0e1c5fb
//    ----------
//    2.1.0
//    blindpirate committed on Dec 4, 2016
//    d968503
//    ----------
//    2.0
//    blindpirate committed on Dec 4, 2016
//    eb20df6
//    ----------
//    1.2.0
//    blindpirate committed on Dec 4, 2016
//    bf90017
//    ----------
//    1.0.0
//    blindpirate committed on Dec 4, 2016
//    ce46284
//    ----------
//    unknown tag
//    blindpirate committed on Dec 4, 2016
//    eef7c7d
//    ----------
//    0.0.3-prerelease
//    blindpirate committed on Dec 4, 2016
//    9390132
//    ----------
//    v0.0.2
//    blindpirate committed on Dec 4, 2016
//    1002ec6
//    ----------
//    0.0.1
//    blindpirate committed on Dec 4, 2016
//    396856c
//    ----------
//    helloworld.go
//    blindpirate committed on Dec 4, 2016
//    a16f45a
//    ----------
//    Update README.md
//    blindpirate committed on GitHub on Dec 4, 2016
//    8492cf3
//    ----------
//    Initial commit
//    blindpirate committed on Dec 4, 2016

    private static final String INITIAL_COMMIT = "b12418e026113005c55a5f52887f3d314f8e5fb1"

    File resource

    GitClientAccessor accessor

    @Before
    void setUp() {
        accessor = new GitClientAccessor(new ProcessUtils())
    }

    @Test
    void 'getting head commit of master branch should succeed'() {
        assert accessor.headCommitOfBranch(resource, 'master').id.length() == 40
    }

    @Test
    void 'getting head commit of master branch after checkout should succeed'() {
        String head = accessor.headCommitOfBranch(resource, 'master').id
        accessor.checkout(resource, '8492cf3')
        assert head == accessor.headCommitOfBranch(resource, 'master').id
    }

    @Test
    void 'getting remote url of repository should succeed'() {
        assert accessor.getRemoteUrl(resource) == 'https://github.com/blindpirate/test-for-gogradle.git'
    }

    @Test
    void 'finding initial commit should succeed'() {
        assert accessor.findCommit(resource, INITIAL_COMMIT).get().id == INITIAL_COMMIT
    }

    @Test
    void 'finding inexistent commit should fail'() {
        assert !accessor.findCommit(resource, 'nonexistence').isPresent()
    }

    @Test
    void 'getting a tag should succeed'() {
        assert accessor.findCommitByTag(resource, '1.0.0').get().id == 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7'
    }

    @Test
    void 'getting an inexistent tag should fail'() {
        assert !accessor.findCommitByTag(resource, 'nonexistence').isPresent()
    }

    @Test
    @AccessWeb
    @WithResource('')
    @WithMockInjector
    void 'cloning with https and submodules should succeed'() {
        // when
        accessor.clone("https://github.com/blindpirate/test-for-gogradle.git", resource)
        // then
        assert new File(resource, '.git').exists()
        assert new File(resource, 'vendor/submodule/LICENSE').exists()
        assert accessor.headCommitOfBranch(resource, 'master').id.length() == 40
    }

    @Test
    void 'reset to initial commit should succeed'() {
        assert new File(resource, 'helloworld.go').exists()

        accessor.checkout(resource, INITIAL_COMMIT)

        assert !new File(resource, 'helloworld.go').exists()
    }

    @Test
    void 'finding tags should succeed'() {
        List<GitMercurialCommit> commits = accessor.getAllTags(resource)
        assert commits.collect {
            it.tag
        } == ['3.0.0', '2.1.2', '2.1.1', '2.1.0', '2.0', '1.2.0', '1.0.0', '0.0.3-prerelease', 'v0.0.2', '0.0.1']
    }

    @Test
    @AccessWeb
    void 'git pull should succeed'() {
        accessor.hardResetAndPull(resource)
        assert !new File(resource, 'tmpfile').exists()
        assert new File(resource, 'helloworld.go').exists()
        assert new File(resource, 'vendor/submodule/LICENSE').exists()
    }

    @Test
    void 'getting commit time of path should succeed'() {
        // 2016/12/4 23:17:38 UTC+8
        assert accessor.lastCommitTimeOfPath(resource, 'helloworld.go') == 1480864658000L
    }

    @Test
    void 'commit time should be the nearest time to current repo snapshot'() {
        long t0 = accessor.lastCommitTimeOfPath(resource, 'README.md')
        accessor.checkout(resource, '1002ec6')
        long t1 = accessor.lastCommitTimeOfPath(resource, 'README.md')
        assert t0 > t1
    }

    @Test(expected = BuildException)
    void 'getting path at a commit when it does not exist should throw an exception'() {
        // helloworld.go didn't exist in initial commit
        accessor.checkout(resource, INITIAL_COMMIT)
        accessor.lastCommitTimeOfPath(resource, 'helloworld.go')
    }


}
