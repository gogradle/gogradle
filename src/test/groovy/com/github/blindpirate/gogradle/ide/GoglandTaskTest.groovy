package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.task.TaskTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class GoglandTaskTest extends TaskTest {
    GoglandTask task

    @Before
    void setUp() {
        task = buildTask(GoglandTask)
    }

    @Test
    void 'gogland task should be executed successfully'() {
        // when
        task.generateXmlsForGogland()
        // then
        verify(goglandIntegration).generateIdeaXmls()
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RENAME_VENDOR_TASK_NAME)
    }

}
