package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.task.TaskTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor

import java.util.function.Consumer

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.isNull
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GoVetTaskTest extends TaskTest {
    GoVetTask task

    @Before
    void setUp() {
        task = buildTask(GoVetTask)

        when(setting.getPackagePath()).thenReturn('github.com/my/package')
    }

    @Test
    void 'it should depend on install tasks'() {
        assertTaskDependsOn(task, RESOLVE_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RESOLVE_BUILD_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'go vet should succeed'() {
        // when
        task.doAddDefaultAction()
        task.actions[0].execute(task)
        ArgumentCaptor captor = ArgumentCaptor.forClass(List)
        // then
        verify(buildManager).go(captor.capture(), isNull(), any(Consumer), any(Consumer), isNull())
        assert captor.value == ['vet', './...']
    }
}
