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
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class VscodeTaskTest extends TaskTest {
    File resource

    VscodeTask task

    String gopath

    @Before
    void setUp() {
        task = buildTask(VscodeTask)
        if (resource != null) {
            when(project.getRootDir()).thenReturn(resource)
            gopath = StringUtils.toUnixString(new File(resource, '.gogradle/project_gopath'))
            when(buildManager.getProjectGopath()).thenReturn(gopath)
        }
    }

    @Test
    void 'it should depend on installTestDependencies and renameVendorDependencies'() {
        assertTaskDependsOn(task, VENDOR_TASK_NAME)
    }


    @WithResource('')
    @Test
    void 'adding project gopath to settings.json should succeed when it exists'() {
        // given
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

    @WithResource('')
    @Test
    void 'adding project gopath to settings.json should succeed when it does not exist'() {
        // when
        task.addGopathToSettingsDotJson()

        // then
        assert DataExchange.parseJson(new File(resource, '.vscode/settings.json'), Map) == ['go.gopath': gopath]
    }
}
