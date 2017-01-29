package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.AccessWeb
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GitRepositoryHandler
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.vcs.git.GitAccessor
import com.github.blindpirate.gogradle.vcs.git.GitRepository
import org.eclipse.jgit.lib.Repository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource("test-for-gogradle.zip")
class GitAccessorTest {

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

    // injected by GogradleRunner
    File resource

    GitAccessor gitAccessor
    Repository repository

    GitRepositoryHandler golangRepositoryHandler = new GitRepositoryHandler()

    @Before
    void setUp() {
        gitAccessor = new GitAccessor(golangRepositoryHandler)
        repository = gitAccessor.getRepository(resource)
    }

    @Test
    void 'getting head commit of master branch should succeed'() {
        assert gitAccessor.headCommitOfBranch(repository, 'master').isPresent()
    }

    @Test
    void 'getting head commit of master branch after checkout should succeed'() {
        String head = gitAccessor.headCommitOfBranch(repository, 'master').get().getName()
        gitAccessor.checkout(repository, '8492cf3')
        assert head == gitAccessor.headCommitOfBranch(repository, 'master').get().getName()
    }

    @Test
    void 'getting remote urls of repository should succeed'() {
        assert gitAccessor.getRemoteUrl(resource).contains('https://github.com/blindpirate/test-for-gogradle.git')
    }

    @Test
    void 'getting remote url of repository should succeed'() {
        assert gitAccessor.getRemoteUrl(repository) == "https://github.com/blindpirate/test-for-gogradle.git"
    }

    @Test
    void 'finding initial commit should succeed'() {
        assert gitAccessor.findCommit(repository, INITIAL_COMMIT).isPresent()
    }

    @Test
    void 'finding inexistent commit should fail'() {
        assert !gitAccessor.findCommit(repository, 'nonexistence').isPresent()
    }

    @Test
    void 'getting a tag should succeed'() {
        assert gitAccessor.findCommitByTag(repository, '1.0.0').get().name() == 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7'
    }

    @Test
    void 'getting an inexistent tag should fail'() {
        assert !gitAccessor.findCommitByTag(repository, 'nonexistence').isPresent()
    }

    @Test
    @AccessWeb
    @WithResource('')
    void 'cloning with https should succeed'() {
        gitAccessor.cloneWithUrl(null, "https://github.com/blindpirate/test-for-gogradle.git", resource)
        assert new File(resource, '.git').exists()
        assert gitAccessor.headCommitOfBranch(repository, 'master').isPresent()
    }


    @Test
    void 'reset to initial commit should succeed'() {
        assert new File(resource, 'helloworld.go').exists()

        gitAccessor.checkout(repository, INITIAL_COMMIT)

        assert !new File(resource, 'helloworld.go').exists()
    }

    @Test
    void 'finding commit by sem version should succeed'() {
        String commidId = gitAccessor.findCommitBySemVersion(repository, '1.0.0').get().name()
        assert commidId == 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7'
    }

    @Test
    void 'finding commid by sem version expression should succeed'() {
        //3.0.0
        assert semVersionMatch('3.x', '4a06b73b6464f06d64efc53ae9b497f6b9a1ef4f')
        // NOT 1.0.0
        assert !semVersionMatch('!(1.0.0)', 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7')

        // 3.0.0
        assert semVersionMatch('2.0-3.0', '4a06b73b6464f06d64efc53ae9b497f6b9a1ef4f')

        // 2.1.2
        assert semVersionMatch('~2.1.0', '06325a95cbdfb9aecafd804905ab4fa05639ae3f')
        // 1.2.0
        assert semVersionMatch('>=1.0.0 & <2.0.0', 'bf90017e8dd41e9f781d138d5d04ef21ce554824')
    }

    @Test
    @AccessWeb
    void 'git reset --hard HEAD && git pull should succeed'() {
        new File(resource, 'tmpfile').createNewFile()
        gitAccessor.hardResetAndPull(null, repository)

        assert !new File(resource, 'tmpfile').exists()
        assert new File(resource, 'helloworld.go').exists()
    }

    @Test
    void 'getting commit time of path should succeed'() {
        // 2016/12/4 23:17:38 UTC+8
        assert gitAccessor.lastCommitTimeOfPath(repository, 'helloworld.go') == 1480864658000L
    }

    @Test
    void 'commit time should be the nearest time to current repo snapshot'() {
        long t0 = gitAccessor.lastCommitTimeOfPath(repository, 'README.md')
        gitAccessor.checkout(repository, '1002ec6')
        long t1 = gitAccessor.lastCommitTimeOfPath(repository, 'README.md')
        assert t0 > t1
    }

    @Test(expected = IllegalStateException)
    void 'getting path at a commit when it does not exist should throw an exception'() {
        // helloworld.go didn't exist in initial commit
        gitAccessor.checkout(repository, INITIAL_COMMIT)
        gitAccessor.lastCommitTimeOfPath(repository, 'helloworld.go')
    }

    @Test
    @AccessWeb
    @WithResource('')
    void 'cloning and pulling a proxy repository should succeed'() {
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=465167
        gitAccessor.cloneWithUrl('gopkg.in/ini.v1', 'https://gopkg.in/ini.v1', resource)
        assert new File(resource, 'README.md').exists()
        assert gitAccessor.headCommitOfBranch(repository, 'master')
    }

    def semVersionMatch(String semVersion, String resultCommit) {
        return gitAccessor.findCommitBySemVersion(repository, semVersion).get().name() == resultCommit
    }

    /**
     * The following test uses my own private key and username/password to access a private repo,
     * which will be ignored on other ci platform
     */
    @Test
    @AccessWeb
    @WithResource('')
    void 'cloning with ssh private key should succeed'() {
        if (System.getenv('MY_OWN_PRIVATE_KEY')) {
            GitRepository gitRepo = new GitRepository()
            gitRepo.all()
            gitRepo.privateKeyFile(System.getenv('MY_OWN_PRIVATE_KEY'))
            addOneRepo(gitRepo)

            gitAccessor.cloneWithUrl('name', 'git@github.com:adieu/archon-ui-ruff.git', resource)
            gitAccessor.hardResetAndPull('name', gitAccessor.getRepository(resource))
            assert new File(resource, 'README.md').exists()
        }
    }

    @Test
    @AccessWeb
    @WithResource('')
    void 'cloning with username and password should succeed'() {
        if (System.getenv('MY_GITHUB_PASSWORD')) {
            GitRepository gitRepo = new GitRepository()
            gitRepo.all()
            gitRepo.username(System.getenv('MY_GITHUB_USERNAME'))
            gitRepo.password(System.getenv('MY_GITHUB_PASSWORD'))
            addOneRepo(gitRepo)

            gitAccessor.cloneWithUrl('name', 'https://github.com/adieu/archon-ui-ruff.git', resource)
            gitAccessor.hardResetAndPull('name', gitAccessor.getRepository(resource))
            assert new File(resource, 'README.md').exists()
        }
    }

    void addOneRepo(GitRepository repository) {
        ReflectionUtils.getField(golangRepositoryHandler, 'gitRepositories').add(repository)
    }
}
