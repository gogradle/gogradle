package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
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
class TestTaskTest extends TaskTest {
    TestTask task

    File resource

    @Before
    void setUp() {
        task = buildTask(TestTask)
        when(project.getRootDir()).thenReturn(resource)
    }

    @Test
    void 'test task should depend on install task'() {
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'all package should be tested if not specified'() {
        // when
        task.addDefaultActionIfNoCustomActions()
        task.actions[0].execute(task)
        // then
        verify(buildManager).go(['test', './...'], null)
    }

    @Test
    void 'test with specific pattern should succeed'() {
        // given
        File a1 = IOUtils.write(resource, 'a/a1_test.go', '')
        File a2 = IOUtils.write(resource, 'a/a2_test.go', '')
        File a3 = IOUtils.write(resource, 'a/a3.go', '')
        File b1 = IOUtils.write(resource, 'b/b1_test.go', '')
        File b2 = IOUtils.write(resource, 'b/b2.go', '')
        // when
        task.setTestNamePattern(['*_test*'])
        task.addDefaultActionIfNoCustomActions()
        task.actions.each {it.execute(task)}
        // then
        verify(buildManager).go(['test', b1.absolutePath, b2.absolutePath], null)
        verify(buildManager).go(['test', a1.absolutePath, a2.absolutePath, a3.absolutePath], null)
    }
}
