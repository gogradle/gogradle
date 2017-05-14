/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
