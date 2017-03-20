package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.CompressUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import java.nio.file.Path
import java.nio.file.Paths

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely

// Every time we find a @WithResource, that resource will be copyed(or unzipped) to a temp dir
// At the end of that method, these resource will be  destroyed
class WithResourceProcessor extends GogradleRunnerProcessor<WithResource> {
    File resourceDir

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithResource annotation) {
        setUpResource(annotation.value())
        ReflectionUtils.setFieldSafely(instance, 'resource', resourceDir)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithResource annotation) {
        deleteQuitely(resourceDir)
    }

    File setUpResource(String resourceName) {
        File destDir = tmpRandomDirectory("resource")
        // when resource path is empty, the new created empty dir will be used
        if (resourceName.endsWith('zip')) {
            decompressResourceToDir(resourceName, destDir)
        } else if (resourceName != '') {
            copyResourceToDir(resourceName, destDir)
        }

        resourceDir = destDir
    }

    def copyResourceToDir(String resourceName, File destDir) {
        Path source = Paths.get(this.class.classLoader.getResource(resourceName).toURI())
        IOUtils.copyDirectory(source.toFile(), destDir)
    }

    def decompressResourceToDir(String resourceName, File destDir) {
        File resource = new File(this.class.classLoader.getResource(resourceName).toURI())
        CompressUtils.decompressZipOrTarGz(resource, destDir)
    }
}
