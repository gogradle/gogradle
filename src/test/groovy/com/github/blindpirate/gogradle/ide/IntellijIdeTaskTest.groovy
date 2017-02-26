package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.task.TaskTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class IntellijIdeTaskTest extends TaskTest {
    IntellijIdeTask task

    @Before
    void setUp() {
        task = buildTask(IntellijIdeTask)
    }

    @Test
    void 'gogland task should be executed successfully'() {
        // when
        task.generateXmls()
        // then
        verify(goglandIntegration).generateXmls()
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RENAME_VENDOR_TASK_NAME)
    }

}
