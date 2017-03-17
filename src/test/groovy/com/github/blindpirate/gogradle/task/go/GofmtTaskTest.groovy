package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class GofmtTaskTest extends TaskTest {
    GofmtTask task

    File resource

    @Before
    void setUp() {
        task = buildTask(GofmtTask)

        IOUtils.write(resource, 'go/bin/go', '')
        IOUtils.write(resource, 'go/bin/gofmt', '')
        when(goBinaryManager.getBinaryPath()).thenReturn(resource.toPath().resolve('go/bin/go'))
    }

    @Test
    void 'it should depend on install tasks'() {
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'gofmt should succeed'() {
        // when
        task.doAddDefaultAction()
        task.actions[0].execute(task)

        // then
        verify(buildManager).run([new File(resource, 'go/bin/gofmt').absolutePath, '-w', '.'], null)
    }

    @Test
    void 'customized action should be executed successfully'() {
        // when
        task.gofmt 'whatever'
        task.actions[0].execute(task)
        // then
        verify(buildManager).run([new File(resource, 'go/bin/gofmt').absolutePath, 'whatever'], null)
    }
}
