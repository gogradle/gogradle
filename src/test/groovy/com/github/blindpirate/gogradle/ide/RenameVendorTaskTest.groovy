package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.util.IOUtils
import groovy.time.TimeCategory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class RenameVendorTaskTest extends TaskTest {
    RenameVendorTask task
    File resource

    @Before
    void setUp() {
        task = buildTask(RenameVendorTask)
        when(project.getRootDir()).thenReturn(resource)
    }

    @Test
    void 'vendor should be renamed successfully'() {
        // given
        IOUtils.mkdir(resource, 'vendor')
        // when
        task.renameVendor()
        // then
        assert resource.list().first() ==~ /.vendor\d{14}/
        assert !new File(resource, 'vendor').exists()
    }

    @Test
    void 'nothing should happen if vendor does not exist'() {
        task.renameVendor()
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if renaming failed'() {
        // given
        Date now = new Date()
        Date now_1s
        Date now_2s

        use(TimeCategory) {
            now_1s = now + 1.second
            now_2s = now + 2.second
        }

        [now, now_1s, now_2s].each {
            IOUtils.write(resource, ".vendor${it.format('yyyyMMddHHmmss')}", '')
        }


        IOUtils.mkdir(resource, 'vendor')
        println(resource.list())
        task.renameVendor()
    }
}
