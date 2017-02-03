package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class TestTaskTest extends TaskTest {
    TestTask task

    @Before
    void setUp() {
        task = buildTask(TestTask)
    }

    @Test
    void 'test task should depend on install task'() {
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'test task should be executed properly'() {
        // when
        task.test()
        // then
        verify(buildManager).test()
    }

    @Test
    void 'test task should be executed with pattern when provided'() {
        // given
        task.setTestNamePattern(['pattern'])
        // when
        task.test()
        // then
        verify(buildManager).testWithPatterns(['pattern'])
    }
}
