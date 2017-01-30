package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import net.lingala.zip4j.core.ZipFile
import org.junit.runners.model.FrameworkMethod

import java.nio.file.Path
import java.nio.file.Paths

import static com.github.blindpirate.gogradle.GogradleRunner.*
import static com.github.blindpirate.gogradle.GogradleRunner.findAnno
import static com.github.blindpirate.gogradle.util.IOUtils.forceDelete

// Every time we find a @WithResource, that resource will be copyed(or unzipped) to a temp dir
// At the end of that method, these resource will be  destroyed
class WithResourceProcessor extends GogradleRunnerProcessor {
    File resourceDir

    @Override
    void beforeTest(Object instance, FrameworkMethod method) {
        WithResource anno = findAnno(method, WithResource)
        setUpResource(anno.value())
        ReflectionUtils.setFieldSafely(instance, 'resource', resourceDir)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method) {
        forceDelete(resourceDir)
    }

    File setUpResource(String resourceName) {
        File destDir = tmpRandomDirectory("resource")
        // when resource path is empty, the new created empty dir will be used
        if (resourceName.endsWith('zip')) {
            unzipResourceToDir(resourceName, destDir)
        } else if (resourceName != '') {
            copyResourceToDir(resourceName, destDir)
        }

        resourceDir = destDir
    }

    def copyResourceToDir(String resourceName, File destDir) {
        Path source = Paths.get(this.class.classLoader.getResource(resourceName).toURI())
        IOUtils.copyDirectory(source.toFile(), destDir)
    }

    def unzipResourceToDir(String resourceName, File destDir) {
        URI uri = this.class.classLoader.getResource(resourceName).toURI()
        ZipFile zipFile = new ZipFile(new File(uri))
        zipFile.extractAll(destDir.toString())
    }
}
