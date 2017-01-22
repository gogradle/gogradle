package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.GogradleGlobal.GOGRADLE_BUILD_DIR_NAME
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class CleanTaskTest extends TaskTest {

    CleanTask task

    File resource

    @Before
    void setUp() {
        task = buildTask(CleanTask)
        when(project.getRootDir()).thenReturn(resource)
    }

    @Test
    void 'clean should succeed when .gogradle exists'() {
        // given
        IOUtils.mkdir(resource, "${GOGRADLE_BUILD_DIR_NAME}/dir")
        IOUtils.write(resource, "${GOGRADLE_BUILD_DIR_NAME}/file", '')
        // when
        task.clean()
        // then
        assert !new File(resource, "${GOGRADLE_BUILD_DIR_NAME}/dir").exists()
        assert !new File(resource, "${GOGRADLE_BUILD_DIR_NAME}/file").exists()
    }


    @Test
    void 'clean should succeed when .gogradle does not exit'() {
        // when
        task.clean()
        // then
        assert !new File(resource, GOGRADLE_BUILD_DIR_NAME).exists()
    }

}
