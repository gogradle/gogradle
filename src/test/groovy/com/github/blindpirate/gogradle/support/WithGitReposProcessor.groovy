package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.Assert
import com.github.blindpirate.gogradle.util.CompressUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely

class WithGitReposProcessor extends GogradleRunnerProcessor<WithGitRepos> {
    File resourceDir
    GitServer gitServer = GitServer.newServer()

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithGitRepos annotation) {
        setUpGitServer(annotation.value())
        ReflectionUtils.setFieldSafely(instance, 'repositories', resourceDir)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithGitRepos annotation) {
        gitServer.stop()
        deleteQuitely(resourceDir)
    }

    File setUpGitServer(String resourceName) {
        File destDir = tmpRandomDirectory("repositories")
        Assert.isTrue(resourceName.endsWith('.zip'))
        decompressResourceToDir(resourceName, destDir)
        resourceDir = destDir

        resourceDir.listFiles().each {
            if (it.isDirectory()) {
                gitServer.addRepo(it.name, it)
            }
        }

        gitServer.start(GitServer.DEFAULT_PORT)
    }

    def decompressResourceToDir(String resourceName, File destDir) {
        File resource = new File(this.class.classLoader.getResource(resourceName).toURI())
        CompressUtils.decompressZipOrTarGz(resource, destDir)
    }
}
