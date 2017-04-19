package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class PrepareTaskTest extends TaskTest {

    PrepareTask task

    File resource

    @Before
    void setUp() {
        task = buildTask(PrepareTask)
        when(project.getRootDir()).thenReturn(resource)
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

}
