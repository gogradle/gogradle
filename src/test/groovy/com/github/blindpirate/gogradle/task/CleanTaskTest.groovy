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
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.GogradleGlobal.GOGRADLE_BUILD_DIR_NAME
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class CleanTaskTest extends TaskTest {

    CleanTask task

    File resource

    @Before
    void setUp() {
        task = buildTask(CleanTask)
        when(project.getProjectDir()).thenReturn(resource)
    }

    @Test
    void 'clean should succeed when .gogradle exists'() {
        // given
        IOUtils.mkdir(resource, "${GOGRADLE_BUILD_DIR_NAME}/dir")
        IOUtils.write(resource, "${GOGRADLE_BUILD_DIR_NAME}/file", '')
        // when
        task.clean()
        // then
        assert !new File(resource, "${GOGRADLE_BUILD_DIR_NAME}/dir").exists()
        assert !new File(resource, "${GOGRADLE_BUILD_DIR_NAME}/file").exists()
    }


    @Test
    void 'clean should succeed when .gogradle does not exit'() {
        // when
        task.clean()
        // then
        assert !new File(resource, GOGRADLE_BUILD_DIR_NAME).exists()
    }

}
