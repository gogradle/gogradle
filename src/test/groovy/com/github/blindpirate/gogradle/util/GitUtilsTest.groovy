package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.AccessWeb
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import org.eclipse.jgit.lib.Repository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.FileUtils.forceDelete

@RunWith(GogradleRunner)
@WithResource("test-for-gogradle.zip")
class GitUtilsTest {

    private static final String INITIAL_COMMIT = "b12418e026113005c55a5f52887f3d314f8e5fb1"

    // injected by GogradleRunner
    File resource

    GitUtils gitUtils = new GitUtils();
    Repository repository

    @Before
    public void setUp() {
        repository = gitUtils.getRepository(resource.toPath())
    }

    @Test
    public void 'getting head commit of master branch should success'() {
        assert gitUtils.headCommitOfBranch(repository, 'master')
    }

    // TODO multiple urls
    @Test
    public void 'getting remote url of repository should success'() {
        assert gitUtils.getRemoteUrl(repository).contains("https://github.com/blindpirate/test-for-gogradle.git")
    }

    @Test
    public void 'finding initial commit should success'() {
        assert gitUtils.findCommit(repository, INITIAL_COMMIT).isPresent()
    }

    @Test
    public void 'finding inexistent commit should fail'() {
        assert !gitUtils.findCommit(repository, 'nonexistence').isPresent()
    }

    @Test
    public void 'getting a tag should success'() {
        assert gitUtils.findCommitByTag(repository, '1.0.0').get() == 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7'
    }

    @Test
    public void 'getting an inexistent tag should fail'() {
        assert !gitUtils.findCommitByTag(repository, 'nonexistence').isPresent()
    }

    @Test
    @AccessWeb
    public void 'clone with https should success'() {
        File tmpDir = new File("build/tmp/nonexistent-${UUID.randomUUID()}")

        gitUtils.cloneWithUrl("https://github.com/blindpirate/test-for-gogradle.git", tmpDir.toPath());

        assert tmpDir.toPath().resolve('.git').toFile().exists()
        forceDelete(tmpDir)
    }

    @Test
    public void 'reset to initial commit should success'() {
        assert resource.toPath().resolve('helloworld.go').toFile().exists()

        gitUtils.resetToCommit(repository, INITIAL_COMMIT)

        assert !resource.toPath().resolve('helloworld.go').toFile().exists()
    }

    @Test
    public void 'finding commit by sem version should success'() {
        String commidId = gitUtils.findCommitBySemVersion(repository, '1.0.0').get()
        assert commidId == 'ce46284fa7c4ff721e1c43346bf19919fa22d5b7'
    }

    @Test
    public void 'finding commid by sem version expression should success'() {
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
    @WithResource('out-of-date-git-repo.zip')
    public void 'git reset --hard HEAD && git pull should success'() {
        resource.toPath().resolve('tmpfile').toFile().createNewFile()
        gitUtils.hardResetAndUpdate(repository)

        assert !resource.toPath().resolve('tmpfile').toFile().exists()
        assert resource.toPath().resolve('helloworld.go').toFile().exists()
    }

    def semVersionMatch(String semVersion, String resultCommit) {
        return gitUtils.findCommitBySemVersion(repository, semVersion).get() == resultCommit
    }
}
