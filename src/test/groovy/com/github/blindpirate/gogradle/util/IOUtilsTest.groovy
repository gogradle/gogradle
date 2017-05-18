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

package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.common.GoSourceCodeFilter
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstallFileFilter
import com.github.blindpirate.gogradle.support.OnlyOnPosix
import com.github.blindpirate.gogradle.support.OnlyOnWindows
import com.github.blindpirate.gogradle.support.WithResource
import org.apache.commons.io.filefilter.TrueFileFilter
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class IOUtilsTest {

    @Mock
    File mockFile

    File resource

    File unexistent = new File('/gogradle_unexistent')

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when forceMkdir fails'() {
        IOUtils.write(resource, 'dir', '')
        IOUtils.forceMkdir(new File(resource, 'dir'))
    }

    @Test
    void 'forceDelete null should succeed'() {
        IOUtils.forceDelete(null)
    }

    @Test(expected = UncheckedIOException)
    void 'deleting unexistent file should throw exception'() {
        IOUtils.forceDelete(unexistent)
    }

    @Test
    void 'checking empty directory should succeed'() {
        assert IOUtils.dirIsEmpty(resource)
    }

    @Test
    void 'checking non-empty directory should succeed'() {
        new File(resource, 'file').createNewFile()
        assert !IOUtils.dirIsEmpty(resource)
    }

    @Test
    void 'safeList should succeed when File.list() return null'() {
        when(mockFile.list()).thenReturn(null)
        assert IOUtils.safeList(mockFile) == []
    }

    @Test
    void 'safeListFiles should succeed when File.listFiles() return null'() {
        when(mockFile.listFiles()).thenReturn(null)
        assert IOUtils.safeListFiles(mockFile) == []
    }

    @Test
    void 'safeList should succeed'() {
        IOUtils.mkdir(resource, 'a')
        assert IOUtils.safeList(resource) == ['a']
    }

    @Test
    void 'validating directory should succeed'() {
        IOUtils.write(resource, 'file', '')
        assert IOUtils.isValidDirectory(resource)
        assert !IOUtils.isValidDirectory(new File(resource, 'unexistent'))
        assert !IOUtils.isValidDirectory(new File(resource, 'file'))
    }

    @Test(expected = IllegalStateException)
    void 'checking a file should throw s exception'() {
        File file = new File(resource, 'file')
        file.createNewFile()
        IOUtils.dirIsEmpty(file)
    }

    @Test
    void 'copying directory should succeed'() {
        IOUtils.write(resource, 'src/file', '')
        IOUtils.mkdir(resource, 'dest')
        IOUtils.copyDirectory(new File(resource, 'src'), new File(resource, 'dest'))
        assert new File(resource, 'dest/file').exists()
    }

    @Test
    void 'copying empty file should succeed'() {
        IOUtils.write(resource, 'src/1.go', '')
        IOUtils.mkdir(resource, 'dest')

        IOUtils.copyDirectory(new File(resource, 'src'), new File(resource, 'dest'), DependencyInstallFileFilter.INSTANCE)
        assert new File(resource, 'dest/1.go').exists()
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when copying src or dest is invalid'() {
        IOUtils.copyDirectory(new File(resource, 'invalid'), new File(resource, 'invalid'))
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when copying src or dest is invalid 2'() {
        IOUtils.copyDirectory(new File(resource, 'invalid'), new File(resource, 'invalid'), TrueFileFilter.INSTANCE)
    }

    @Test
    void 'touching should succeed'() {
        IOUtils.touch(new File(resource, 'newFile'))
        assert new File(resource, 'newFile').exists()
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when touching fails'() {
        when(mockFile.exists()).thenReturn(true)
        when(mockFile.setLastModified(any(long))).thenReturn(false)
        IOUtils.touch(mockFile)
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when writing to data fails'() {
        IOUtils.write(resource, '')
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when trying to get text from invalid file'() {
        IOUtils.toString(new File(resource, 'invalid'))
    }

    @Test
    void 'getting text from file should succeed'() {
        IOUtils.touch(new File(resource, 'file'))
        assert IOUtils.toString(new File(resource, 'file')) == ''
    }

    @Test
    @OnlyOnWindows
    void 'file should be closed after reading'() {
        File file = new File(resource, 'test')
        IOUtils.write(file, '')
        assert IOUtils.toString(file) == ''
        assert file.delete()
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if IOException occurs'() {
        InputStream is = mock(InputStream)
        when(is.read()).thenThrow(new IOException())
        IOUtils.toString(is)
    }

    @Test
    void 'empty list should be returned if file is empty'() {
        IOUtils.write(resource, 'file', '')
        assert IOUtils.readLines(new File(resource, 'file')) == []
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when walking file tree fails'() {
        IOUtils.walkFileTreeSafely(resource.toPath().resolve('invalid'), new SimpleFileVisitor<Path>())
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if clearing directory fails'() {
        when(mockFile.isDirectory()).thenReturn(true)
        when(mockFile.exists()).thenReturn(true)
        IOUtils.clearDirectory(mockFile)
    }

    @Test(expected = UncheckedIOException)
    @OnlyOnPosix
    void 'exception should be thrown when chmod +x fails'() {
        IOUtils.chmodAddX(resource.toPath().resolve('unexistent'))
    }

    @Test
    void 'checking existence and writability of absolute path should succeed'() {
        // given
        Path path = resource.toPath().resolve('a')
        // when
        IOUtils.ensureDirExistAndWritable(path)
        IOUtils.ensureDirExistAndWritable(path)
        // then
        assert path.toFile().exists()
    }

    @Test
    void 'checking existance and writability of base path and path should succeed'() {
        // when
        IOUtils.ensureDirExistAndWritable(resource.toPath(), "a")
        IOUtils.ensureDirExistAndWritable(resource.toPath(), "a")
        // then
        assert new File(resource, 'a').exists()
    }

    @Test(expected = IllegalStateException)
    void 'checking relative path should fail'() {
        IOUtils.ensureDirExistAndWritable(Paths.get('a'), 'b')
    }

    @Test
    void 'write file with complex file name should succeed'() {
        // when
        IOUtils.write(resource, 'a/b/c', 'content')
        // then
        assert new File(resource, 'a').exists()
        assert new File(resource, 'a/b/c').getText() == 'content'
    }

    @Test(expected = UncheckedIOException)
    void 'tracking an unexistent path should fail'() {
        IOUtils.toRealPath(unexistent.toPath())
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if copyFile fails'() {
        IOUtils.copyFile(unexistent, unexistent)
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if countLines fails'() {
        IOUtils.countLines(unexistent.toPath())
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if copyURLToFile fails'() {
        IOUtils.copyURLToFile(new URL('http://unexistent'), unexistent)
    }

    @Test
    void 'clearing an unexistent directory should succeed'() {
        IOUtils.clearDirectory(null)
        IOUtils.clearDirectory(new File('unexistent'))
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if IOException occurs in serializtion'() {
        IOUtils.serialize(1, resource)
    }

    @Test
    void 'existent file should be used as serialization file directly'() {
        IOUtils.write(resource, 'file', '')
        IOUtils.serialize(1, new File(resource, 'file'))
        assert IOUtils.deserialize(new File(resource, 'file')) == 1
    }

}
