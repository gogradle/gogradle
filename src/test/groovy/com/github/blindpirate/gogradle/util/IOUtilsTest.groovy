package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path
import java.nio.file.Paths

@RunWith(GogradleRunner)
@WithResource('')
class IOUtilsTest {
    File resource

    @Test
    void 'checking empty directory should succeed'() {
        assert IOUtils.dirIsEmpty(resource)
    }

    @Test
    void 'checking non-empty directory should succeed'() {
        new File(resource, 'file').createNewFile()
        assert !IOUtils.dirIsEmpty(resource)
    }

    @Test(expected = IllegalStateException)
    void 'checking a file should throw s exception'() {
        File file = new File(resource, 'file')
        file.createNewFile()
        IOUtils.dirIsEmpty(file)
    }

    @Test
    void 'checking existance and writability of absolute path should succeed'() {
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
