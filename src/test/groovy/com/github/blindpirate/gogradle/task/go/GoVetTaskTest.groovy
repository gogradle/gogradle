package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.task.TaskTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME

@RunWith(GogradleRunner)
class GoVetTaskTest extends TaskTest {
    GoVetTask task

    @Before
    void setUp() {
        task = buildTask(GoVetTask)

        Mockito.when(setting.getPackagePath()).thenReturn('github.com/my/package')
    }

    @Test
    void 'it should depend on install tasks'() {
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'go vet should succeed'() {
        // when
        task.doAddDefaultAction()
        task.actions[0].execute(task)
        // then
        Mockito.verify(buildManager).go(['vet', './...'], null, null, null, null)
    }
}
