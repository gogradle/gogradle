/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.util.StringUtils
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
        String projectGopath = new File(resource, 'project/.gogradle/project_gopath').getAbsolutePath().replace('\\', '/')
        when(buildManager.getProjectGopath()).thenReturn(projectGopath)
        when(project.getRootDir()).thenReturn(new File(resource, 'project'))
        when(goBinaryManager.getGoroot()).thenReturn(resource.toPath().resolve('goroot'))
        // when
        task.showGopathGoroot()
        // then
        verify(logger).quiet("GOPATH: {}", projectGopath)
        verify(logger).quiet("GOROOT: {}", StringUtils.toUnixString(new File(resource, 'goroot')))
    }
}
