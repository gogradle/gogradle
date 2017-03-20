package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME
import static com.github.blindpirate.gogradle.util.StringUtils.*
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
        when(project.getRootDir()).thenReturn(resource)
        when(goBinaryManager.getBinaryPath()).thenReturn(resource.toPath().resolve('go/bin/go'))
    }

    @Test
    void 'it should depend on install tasks'() {
        assertTaskDependsOn(task, PREPARE_TASK_NAME)
    }

    @Test
    void 'gofmt should succeed'() {
        // when
        task.doAddDefaultAction()
        task.actions[0].execute(task)

        // then
        verify(buildManager).run([toUnixString(new File(resource, 'go/bin/gofmt')), '-w', toUnixString(resource.getAbsolutePath())]
                , null
                , null
                , null
                , null)
    }

    @Test
    void 'customized action should be executed successfully'() {
        // when
        task.gofmt 'whatever'
        // then
        verify(buildManager).run([toUnixString(new File(resource, 'go/bin/gofmt')), 'whatever'], null, null, null, null)
    }
}
