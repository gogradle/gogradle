package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.api.Task
import org.gradle.api.internal.tasks.TaskContainerInternal
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class PrepareTaskTest extends TaskTest {

    PrepareTask task

    File resource

    @Mock
    TaskContainerInternal taskContainer

    @Before
    void setUp() {
        task = buildTask(PrepareTask)
        when(project.getRootDir()).thenReturn(resource)
        when(project.getTasks()).thenReturn(taskContainer)
        when(setting.getPackagePath()).thenReturn('github.com/my/project')
    }

    @Test
    void 'preparation should succeed'() {
        // when
        task.prepare()
        // then
        verify(setting).verify()
        verify(goBinaryManager).getBinaryPath()
        verify(buildManager).ensureDotVendorDirNotExist()
        verify(buildManager).prepareSymbolicLinks()
        verify(buildConstraintManager).prepareConstraints()
        verify(gogradleRootProject).initSingleton('github.com/my/project', resource)
    }

    @Test
    void 'old gogradle.lock should be removed before locking'() {
        // given
        IOUtils.write(resource, 'gogradle.lock', '')
        when(taskContainer.getByName('goLock')).thenReturn(mock(Task))
        // when
        task.prepare()
        // then
        assert !new File(resource, 'gogradle.lock').exists()
    }

}
