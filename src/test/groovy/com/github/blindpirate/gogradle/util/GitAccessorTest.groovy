package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.AccessWeb
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.vcs.git.GitAccessor
import org.eclipse.jgit.lib.Repository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import java.nio.file.Path

import static IOUtils.forceDelete
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource("test-for-gogradle.zip")
class GitAccessorTest {

    private static final String INITIAL_COMMIT = "b12418e026113005c55a5f52887f3d314f8e5fb1"

    // injected by GogradleRunner
    File resource

    GitAccessor gitAccessor = new GitAccessor();
    Repository repository

    @Before
    void setUp() {
        repository = gitAccessor.getRepository(resource.toPath())
    }

    @Test
    void 'getting head commit of master branch should success'() {
        assert gitAccessor.headCommitOfBranch(repository, 'master')
    }

    @Test
    void 'getting remote urls of repository should success'() {
        assert gitAccessor.getRemoteUrls(repository).contains("https://github.com/blindpirate/test-for-gogradle.git")
        assert gitAccessor.getRemoteUrls(resource.toPath()).contains('https://github.com/blindpirate/test-for-gogradle.git')
    }

    @Test
    void 'getting remote url of repository should success'() {
        assert gitAccessor.getRemoteUrl(repository) == "https://github.com/blindpirate/test-for-gogradle.git"
    }

    @Test
    void 'finding initial commit should success'() {
        assert gitAccessor.findCommit(repository, INITIAL_COMMIT).isPresent()
    }

    @Test
    void 'finding inexistent commit should fail'() {
        assert !gitAccessor.findCommit(repository, 'nonexistence').isPresent()
    }

    @Test
    void 'getting a tag should success'() {
        assert gitAccessor.findCommitByTag(repository, '1.0.0').get().name() == 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7'
    }

    @Test
    void 'getting an inexistent tag should fail'() {
        assert !gitAccessor.findCommitByTag(repository, 'nonexistence').isPresent()
    }

    @Test
    @AccessWeb
    @WithResource('')
    void 'clone with https should success'() {
        gitAccessor.cloneWithUrl("https://github.com/blindpirate/test-for-gogradle.git", resource.toPath());
        assert resource.toPath().resolve('.git').toFile().exists()
    }

    @Test
    void 'reset to initial commit should success'() {
        assert resource.toPath().resolve('helloworld.go').toFile().exists()

        gitAccessor.resetToCommit(repository, INITIAL_COMMIT)

        assert !resource.toPath().resolve('helloworld.go').toFile().exists()
    }

    @Test
    void 'finding commit by sem version should success'() {
        String commidId = gitAccessor.findCommitBySemVersion(repository, '1.0.0').get().name()
        assert commidId == 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7'
    }

    @Test
    void 'finding commid by sem version expression should success'() {
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
    void 'git reset --hard HEAD && git pull should success'() {
        resource.toPath().resolve('tmpfile').toFile().createNewFile()
        gitAccessor.hardResetAndUpdate(repository)

        assert !resource.toPath().resolve('tmpfile').toFile().exists()
        assert resource.toPath().resolve('helloworld.go').toFile().exists()
    }

    def semVersionMatch(String semVersion, String resultCommit) {
        return gitAccessor.findCommitBySemVersion(repository, semVersion).get().name() == resultCommit
    }
}
