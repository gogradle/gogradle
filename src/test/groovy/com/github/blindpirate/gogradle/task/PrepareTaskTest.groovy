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
import org.gradle.api.internal.GradleInternal
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

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
        verify(buildManager).prepareProjectGopathIfNecessary()
        verify(buildConstraintManager).prepareConstraints()
    }

}
