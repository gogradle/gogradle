package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*

@RunWith(GogradleRunner)
class CheckTaskTest extends TaskTest {

    CheckTask task

    @Before
    void setUp() {
        task = buildTask(CheckTask)
    }

    @Test
    void 'check task should depends on test task'() {
        assertTaskDependsOn(task, TEST_TASK_NAME)
    }
}
