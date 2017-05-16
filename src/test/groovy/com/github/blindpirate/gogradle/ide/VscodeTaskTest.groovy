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

package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.GolangTaskContainer
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class VscodeTaskTest extends TaskTest {
    File resource

    VscodeTask task

    @Before
    void setUp() {
        task = buildTask(VscodeTask)
        when(project.getRootDir()).thenReturn(resource)
    }

    @Test
    void 'it should depend on installTestDependencies and renameVendorDependencies'() {
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, RENAME_VENDOR_TASK_NAME)
    }


    @WithResource('')
    @Test
    void 'adding project gopath to settings.json should succeed when it exists'() {
        // given
        String projectGopath = StringUtils.toUnixString(new File(resource, '.gogradle/project_gopath'))
        String buildGopath = StringUtils.toUnixString(new File(resource, '.gogradle/build_gopath'))
        String testGopath = StringUtils.toUnixString(new File(resource, '.gogradle/test_gopath'))
        String gopath = projectGopath + File.pathSeparator + buildGopath + File.pathSeparator + testGopath
        when(buildManager.getTestGopath()).thenReturn(gopath)
        IOUtils.write(new File(resource, '.vscode/settings.json'), '''
// Place your settings in this file to overwrite default and user settings.
{
"hello":"world"
}
''')
        // when
        task.addGopathToSettingsDotJson()

        // then
        assert DataExchange.parseJson(new File(resource, '.vscode/settings.json'), Map) == [hello: 'world', 'go.gopath': gopath]
    }
}
