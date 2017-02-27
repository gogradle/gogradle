package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class ShowGopathGorootTaskTest extends TaskTest {
    ShowGopathGorootTask task

    File resource

    @Mock
    Logger logger

    @Before
    void setUp() {
        task = buildTask(ShowGopathGorootTask)
        ReflectionUtils.setStaticFinalField(ShowGopathGorootTask, 'LOGGER', logger)
    }

    @After
    void cleanUp() {
        ReflectionUtils.setStaticFinalField(ShowGopathGorootTask, 'LOGGER', Logging.getLogger(ShowGopathGorootTask))
    }

    @Test
    @WithResource('')
    void 'it should succeed'() {
        // when
        when(project.getRootDir()).thenReturn(new File(resource, 'project'))
        when(goBinaryManager.getGoroot()).thenReturn(resource.toPath().resolve('goroot'))
        // when
        task.showGopathGoroot()
        // then
        String projectGopath = new File(resource, 'project/.gogradle/project_gopath').getAbsolutePath().replace('\\', '/')
        String buildGopath = new File(resource, 'project/.gogradle/build_gopath').getAbsolutePath().replace('\\', '/')
        String testGopath = new File(resource, 'project/.gogradle/test_gopath').getAbsolutePath().replace('\\', '/')
        String separator = File.pathSeparator
        verify(logger).quiet("GOROOT: {}", new File(resource, 'goroot').absolutePath)
        verify(logger).quiet("GOPATH: {}", "${projectGopath}${separator}${buildGopath}${separator}${testGopath}".toString())
    }
}
