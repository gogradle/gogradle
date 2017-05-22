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
import com.github.blindpirate.gogradle.task.TaskTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor

import java.util.function.Consumer

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME
import static org.mockito.ArgumentMatchers.*
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
        assertTaskDependsOn(task, VENDOR_TASK_NAME)
    }

    @Test
    void 'go vet should succeed'() {
        // when
        task.doAddDefaultAction()
        task.actions[0].execute(task)
        ArgumentCaptor captor = ArgumentCaptor.forClass(List)
        // then
        verify(buildManager).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), isNull())
        assert captor.value == ['vet', 'github.com/my/package/...']
    }
}
