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
        if (annotation.value() != '') {
            setUpGitServerWithZip(annotation.value())
        } else {
            setUpGitServerFromScratch(annotation.repoNames(), annotation.fileNames())
        }
        ReflectionUtils.setFieldSafely(instance, 'repositories', resourceDir)
    }

    def setUpGitServerFromScratch(String[] repoNames, String[] fileNames) {
        resourceDir = tmpRandomDirectory("repositories")
        (0..<repoNames.size()).each {
            File dir = new File(resourceDir, repoNames[it])
            dir.mkdir()
            GitServer.createRepository(dir, fileNames[it])
            gitServer.addRepo(repoNames[it], dir)
        }
        gitServer.start(GitServer.DEFAULT_PORT)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithGitRepos annotation) {
        gitServer.stop()
        deleteQuitely(resourceDir)
    }

    File setUpGitServerWithZip(String resourceName) {
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
