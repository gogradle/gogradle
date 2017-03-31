package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely

class WithGitRepoProcessor extends GogradleRunnerProcessor<WithGitRepo> {
    File repoDir
    GitServer gitServer = GitServer.newServer()

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithGitRepo annotation) {
        setUpGitServer(annotation.repoName(), annotation.fileName())
        ReflectionUtils.setFieldSafely(instance, 'repositories', repoDir)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithGitRepo annotation) {
        gitServer.stop()
        deleteQuitely(repoDir)
    }

    File setUpGitServer(String repoName, String fileName) {
        repoDir = tmpRandomDirectory("repositories")

        GitServer.createRepository(repoDir, fileName)

        gitServer.addRepo(repoName, repoDir)
        gitServer.start(GitServer.DEFAULT_PORT)
    }
}
