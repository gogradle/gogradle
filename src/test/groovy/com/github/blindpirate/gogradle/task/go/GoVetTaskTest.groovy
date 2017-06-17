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

package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor

import java.util.function.Consumer

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class GoVetTaskTest extends TaskTest {
    GoVetTask task

    File resource

    @Before
    void setUp() {
        task = buildTask(GoVetTask)
        when(setting.getPackagePath()).thenReturn('github.com/my/package')
        when(project.getProjectDir()).thenReturn(resource)
        if (resource) {
            IOUtils.mkdir(resource, '.dir')
            IOUtils.mkdir(resource, '_dir')
            IOUtils.mkdir(resource, 'vendor')
            IOUtils.mkdir(resource, 'sub')
        }
    }

    @Test
    void 'it should depend on install tasks'() {
        assertTaskDependsOn(task, VENDOR_TASK_NAME)
    }

    @Test
    @WithResource('')
    void 'go vet should succeed when .go exists in root'() {
        // given
        IOUtils.write(resource, 'main.go', '')
        // when
        task.doAddDefaultAction()
        task.actions.each { it.execute(task) }
        ArgumentCaptor captor = ArgumentCaptor.forClass(List)
        // then
        verify(buildManager, times(2)).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), isNull())

        assert captor.allValues.size() == 2
        List firstCall = captor.allValues[0]
        List secondCall = captor.allValues[1]

        assert firstCall == ['tool', 'vet', toUnixString(new File(resource, 'main.go'))]
        assert secondCall == ['tool', 'vet', toUnixString(new File(resource, 'sub'))]
    }

    @Test
    @WithResource('')
    void 'go vet should succeed when .go not exists in root'() {
        // when
        task.doAddDefaultAction()
        task.actions.each { it.execute(task) }
        ArgumentCaptor captor = ArgumentCaptor.forClass(List)
        // then
        verify(buildManager).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), isNull())
        assert captor.value == ['tool', 'vet', toUnixString(new File(resource, 'sub'))]
    }
}
