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

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class GoVetTest extends TaskTest {
    GoVet task

    File resource

    @Captor
    ArgumentCaptor<List> captor

    @Before
    void setUp() {
        task = buildTask(GoVet)
        when(setting.getPackagePath()).thenReturn('github.com/my/package')
        when(project.getProjectDir()).thenReturn(resource)
        IOUtils.mkdir(resource, '.dir')
        IOUtils.mkdir(resource, '_dir')
        IOUtils.mkdir(resource, 'vendor')
        IOUtils.mkdir(resource, 'sub')
    }

    @Test
    void 'it should depend on install tasks'() {
        assertTaskDependsOn(task, VENDOR_TASK_NAME)
    }

    @Test
    void 'go vet should succeed when go vet not ignore vendor'() {
        // given
        IOUtils.write(resource, 'main.go', '')
        IOUtils.write(resource, 'sub/sub.go', '')
        // when
        when(goBinaryManager.goVetIgnoreVendor()).thenReturn(false)
        task.afterEvaluate()
        task.executeTask()
        // then
        verify(buildManager).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), eq(false))

        assert captor.allValues.size() == 1

        assert captor.allValues[0] == ['vet', 'github.com/my/package', 'github.com/my/package/sub']
    }

    @Test
    void 'go vet should succeed when go vet ignore vendor and global gopath'() {
        // given
        IOUtils.write(resource, 'main.go', '')
        IOUtils.write(resource, 'sub/sub.go', '')
        // when
        when(goBinaryManager.goVetIgnoreVendor()).thenReturn(true)
        when(buildManager.getGopath()).thenReturn("/global/gopath")
        task.afterEvaluate()
        task.executeTask()
        // then
        verify(buildManager).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), eq(false))

        assert captor.allValues.size() == 1

        assert captor.allValues[0] == ['vet', 'github.com/my/package/...']
    }

    @Test
    void 'go vet should succeed when _go not exists in root'() {
        // when
        task.afterEvaluate()
        task.executeTask()
        // then
        verify(buildManager, never()).go(anyList(), anyMap(), any(Consumer), any(Consumer), anyBoolean())
    }

    @Test
    void 'custom action should be executed if specified'() {
        // given
        task.go('tool vet xxx') {
            stderr {}
        }
        task.continueOnFailure = true
        // when
        task.afterEvaluate()
        task.executeTask()
        // then
        ArgumentCaptor captor1 = ArgumentCaptor.forClass(Consumer)
        ArgumentCaptor captor2 = ArgumentCaptor.forClass(Consumer)
        verify(buildManager).go(captor.capture(), anyMap(), captor1.capture(), captor2.capture(), eq(true))
        assert captor.value == ['tool', 'vet', 'xxx']
        assert captor1.value.class.name.contains('Lambda')
        assert captor2.value.class.name.contains('Go$ClosureLineConsumer')
    }
}
