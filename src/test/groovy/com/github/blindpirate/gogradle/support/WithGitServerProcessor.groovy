package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.Assert
import com.github.blindpirate.gogradle.util.CompressUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely

class WithGitServerProcessor extends GogradleRunnerProcessor<WithGitServer> {
    private static final int DEFAULT_PORT = 8080
    File resourceDir
    GitServer gitServer = GitServer.newServer()

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithGitServer annotation) {
        setUpGitServer(annotation.value())
        ReflectionUtils.setFieldSafely(instance, 'repositories', resourceDir)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithGitServer annotation) {
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

        gitServer.start(DEFAULT_PORT)
    }

    def decompressResourceToDir(String resourceName, File destDir) {
        File resource = new File(this.class.classLoader.getResource(resourceName).toURI())
        CompressUtils.decompressZipOrTarGz(resource, destDir)
    }
}
