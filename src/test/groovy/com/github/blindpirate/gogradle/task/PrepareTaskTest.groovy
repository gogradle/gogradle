package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class PrepareTaskTest extends TaskTest {

    PrepareTask task

    @Before
    void setUp() {
        task = buildTask(PrepareTask)
    }

    @Test
    void 'preparation should succeed'() {
        // when
        task.prepare()
        // then
        verify(goBinaryManager).getBinaryPath()
        verify(buildManager).ensureDotVendorDirNotExist()
        verify(buildManager).prepareSymbolicLinks()
        verify(buildConstraintManager).prepareConstraints()
    }

}
