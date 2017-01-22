package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class BuildTaskTest extends TaskTest {
    BuildTask task

    @Before
    void setUp() {
        task = buildTask(BuildTask)
    }

    @Test
    void 'build task should be executed successfully'() {
        // when
        task.build()
        // then
        verify(buildManager).build()
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
    }
}
