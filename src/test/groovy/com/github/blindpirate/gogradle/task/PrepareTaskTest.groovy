package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.api.Task
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.tasks.TaskContainerInternal
import org.gradle.api.invocation.Gradle
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.Mock

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class PrepareTaskTest extends TaskTest {

    PrepareTask task

    File resource

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    GradleInternal gradle

    @Before
    void setUp() {
        task = buildTask(PrepareTask)
        when(project.getRootDir()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn('github.com/my/project')
        when(project.getGradle()).thenReturn(gradle)
    }

    @Test
    void 'preparation should succeed'() {
        // when
        task.prepare()
        // then
        verify(setting).verify()
        verify(goBinaryManager).getBinaryPath()
        verify(buildManager).ensureDotVendorDirNotExist()
        verify(buildManager).prepareSymbolicLinks()
        verify(buildConstraintManager).prepareConstraints()
        verify(gogradleRootProject).initSingleton('github.com/my/project', resource)
    }

    @Test
    void 'old gogradle.lock should be removed before locking'() {
        // given
        IOUtils.write(resource, 'gogradle.lock', '')
        when(gradle.getStartParameter().getTaskNames()).thenReturn(["goLock"])
        // when
        task.prepare()
        // then
        assert !new File(resource, 'gogradle.lock').exists()

        // given
        IOUtils.write(resource, 'gogradle.lock', '')
        when(gradle.getStartParameter().getTaskNames()).thenReturn(["gL"])
        // when
        task.prepare()
        // then
        assert !new File(resource, 'gogradle.lock').exists()

        // given
        IOUtils.write(resource, 'gogradle.lock', '')
        when(gradle.getStartParameter().getTaskNames()).thenReturn(["gD"])
        // when
        task.prepare()
        // then
        assert new File(resource, 'gogradle.lock').exists()
    }

}
