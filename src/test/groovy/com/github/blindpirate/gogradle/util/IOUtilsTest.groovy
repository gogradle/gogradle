package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path
import java.nio.file.Paths

@RunWith(GogradleRunner)
@WithResource('')
class IOUtilsTest {
    File resource

    @Test
    void 'checking empty directory should success'() {
        assert IOUtils.dirIsEmpty(resource)
    }

    @Test
    void 'checking non-empty directory should success'() {
        resource.toPath().resolve('file').toFile().createNewFile()
        assert !IOUtils.dirIsEmpty(resource)
    }

    @Test(expected = IllegalStateException)
    void 'checking a file should throw s exception'() {
        Path filePath = resource.toPath().resolve('file')
        filePath.toFile().createNewFile()
        IOUtils.dirIsEmpty(filePath.toFile())
    }

    @Test
    void 'checking existance and writability of absolute path should success'() {
        // given
        Path path = resource.toPath().resolve('a')
        // when
        IOUtils.ensureDirExistAndWritable(path)
        IOUtils.ensureDirExistAndWritable(path)
        // then
        assert path.toFile().exists()
    }

    @Test
    void 'checking existance and writability of base path and path should success'() {
        // when
        IOUtils.ensureDirExistAndWritable(resource.toPath(), "a")
        IOUtils.ensureDirExistAndWritable(resource.toPath(), "a")
        // then
        assert resource.toPath().resolve('a').toFile().exists()
    }

    @Test(expected = IllegalStateException)
    void 'checking relative path should fail'() {
        IOUtils.ensureDirExistAndWritable(Paths.get('a'), 'b')
    }

    @Test
    void 'write file with complex file name should success'() {
        // when
        IOUtils.write(resource, 'a/b/c', 'content')
        // then
        assert resource.toPath().resolve('a').toFile().exists()
        assert resource.toPath().resolve('a/b/c').toFile().getText() == 'content'
    }


}
