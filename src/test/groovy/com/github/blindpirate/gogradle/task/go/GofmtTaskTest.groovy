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
import org.mockito.Captor

import java.util.function.Consumer

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class GofmtTaskTest extends TaskTest {
    GofmtTask task

    File resource

    @Captor
    ArgumentCaptor captor

    @Before
    void setUp() {
        task = buildTask(GofmtTask)

        IOUtils.write(resource, '.go/bin/go', '')
        IOUtils.write(resource, '.go/bin/gofmt', '')
        when(project.getRootDir()).thenReturn(resource)
        when(goBinaryManager.getBinaryPath()).thenReturn(resource.toPath().resolve('.go/bin/go'))

        IOUtils.write(resource, 'a.go', '')
        IOUtils.write(resource, '.a.go', '')
        IOUtils.mkdir(resource, 'b')
        IOUtils.mkdir(resource, 'vendor')
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
        verify(buildManager).run(captor.capture(), anyMap(), any(Consumer), any(Consumer), isNull())

        assert captor.value[0..1] == [absolutePath('.go/bin/gofmt'), '-w']
        assert captor.value.contains(absolutePath('a.go'))
        assert captor.value.contains(absolutePath('b'))
    }

    private String absolutePath(String fileName) {
        return toUnixString(new File(resource, fileName))
    }

    @Test
    void 'customized action should be executed successfully'() {
        // when
        task.gofmt 'whatever'
        // then
        verify(buildManager).run(captor.capture(), anyMap(), any(Consumer), any(Consumer), isNull())
        assert captor.value == [absolutePath('.go/bin/gofmt'), 'whatever']
    }
}
