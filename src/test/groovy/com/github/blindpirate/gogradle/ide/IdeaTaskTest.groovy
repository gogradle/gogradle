package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.task.TaskTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class IdeaTaskTest extends TaskTest {
    IdeaTask task

    @Before
    void setUp() {
        task = buildTask(IdeaTask)
    }

    @Test
    void 'idea task should be executed successfully'() {
        // when
        task.generateXmlsForIdea()
        // then
        verify(ideaIntegration).generateXmls()
        assertTaskDependsOn(task, RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RENAME_VENDOR_TASK_NAME)
    }
}
