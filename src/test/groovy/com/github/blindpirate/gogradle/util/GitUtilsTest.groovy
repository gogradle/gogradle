package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.AccessWeb
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import org.eclipse.jgit.lib.Repository
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.FileUtils.forceDelete

@RunWith(GogradleRunner)
@WithResource("test.zip")
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
        assert gitUtils.findCommitByTag(repository, '1.0.0').isPresent()
    }

    @Test
    public void 'getting an inexistent tag should fail'() {
        assert !gitUtils.findCommitByTag(repository, 'nonexistence').isPresent()
    }

    @Test
    @AccessWeb
    @Ignore
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


}
