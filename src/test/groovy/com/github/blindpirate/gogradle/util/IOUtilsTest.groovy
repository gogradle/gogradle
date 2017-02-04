package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
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

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when forceMkdir fails'() {
        IOUtils.write(resource, 'dir', '')
        IOUtils.forceMkdir(new File(resource, 'dir'))
    }

    @Test
    void 'forceDelete null should succeed'() {
        IOUtils.forceDelete(null)
    }

    @Test(expected = IllegalStateException)
    void 'deleting unexistent file should throw exception'() {
        IOUtils.forceDelete(new File(resource, 'unexistent'))
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
    void 'safeList should succeed'() {
        when(mockFile.list()).thenReturn(null)
        assert IOUtils.safeList(mockFile) == []
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

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when copying src or dest is invalid'() {
        IOUtils.copyDirectory(new File(resource, 'invalid'), new File(resource, 'invalid'))
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when copying src or dest is invalid 2'() {
        IOUtils.copyDirectory(new File(resource, 'invalid'), new File(resource, 'invalid'), TrueFileFilter.INSTANCE)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when touching fails'() {
        when(mockFile.exists()).thenReturn(true)
        when(mockFile.setLastModified(any(long))).thenReturn(false)
        IOUtils.touch(mockFile)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when writing to data fails'() {
        IOUtils.write(resource, '')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when trying to get text from invalid file'() {
        IOUtils.toString(new File(resource, 'invalid'))
    }

    @Test
    @OnlyOnWindows
    void 'file should be closed after reading'() {
        File file = new File(resource, 'test')
        IOUtils.write(file, '')
        assert IOUtils.toString(file) == ''
        assert file.delete()
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if IOException occurs'() {
        InputStream is = mock(InputStream)
        when(is.read()).thenThrow(new IOException())
        IOUtils.toString(is)
    }

    @Test
    void 'empty list should be returned if file is empty'() {
        IOUtils.write(resource, 'file', '')
        assert IOUtils.getLines(new File(resource, 'file')) == []
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown when walking file tree fails'() {
        IOUtils.walkFileTreeSafely(resource.toPath().resolve('invalid'), new SimpleFileVisitor<Path>())
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if clearing directory fails'() {
        when(mockFile.isDirectory()).thenReturn(true)
        when(mockFile.exists()).thenReturn(true)
        IOUtils.clearDirectory(mockFile)
    }

    @Test(expected = IllegalStateException)
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


}
