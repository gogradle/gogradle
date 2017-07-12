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
import com.github.blindpirate.gogradle.build.BuildManager
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.StringUtils.*
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class IdeaIntegrationTest {
    @Mock
    GoBinaryManager manager
    @Mock
    Project project
    @Mock
    IdeaSdkHacker hacker
    @Mock
    BuildManager buildManager

    File resource

    IdeaIntegration ideaIntegration

    @Before
    void setUp() {
        ideaIntegration = new IdeaIntegration(manager, project, buildManager, hacker)
        when(project.getProjectDir()).thenReturn(resource)
        when(manager.getBinaryPath()).thenReturn(new File(resource, 'go/bin/go').toPath())
        when(manager.getGoroot()).thenReturn(new File(resource, 'go').toPath())
        when(manager.getGoVersion()).thenReturn('1.7.1')

        when(project.getName()).thenReturn('MyAwesomeProject')
        when(buildManager.getGopath()).thenReturn('')
    }

    @Test
    void 'idea xmls should be generated correctly'() {
        ideaIntegration.generateXmls()

        verify(hacker).ensureSpecificSdkExist('1.7.1', new File(resource, 'go').toPath())


        String moduleIml = new File(resource, '.idea/modules/MyAwesomeProject.iml').text
        assert moduleIml.contains('GO_MODULE')
        assert moduleIml.contains('Go 1.7.1')
        assert moduleIml.contains('Go SDK')

        String modulesXml = new File(resource, '.idea/modules.xml').text
        assert modulesXml.contains('.idea/modules/MyAwesomeProject.iml')
    }

    @Test
    void 'goLibraries should be correct if global GOPATH exists'() {
        when(buildManager.getGopaths()).thenReturn([resource.toPath(), resource.toPath()])
        ideaIntegration.generateXmls()
        assert new File(resource, '.idea/goLibraries.xml').text.count("\"file://${toUnixString(resource)}\"")
    }

    @Test
    void 'goLibraries should be correct if global GOPATH not exists'() {
        when(buildManager.getGopaths()).thenReturn([new File(resource, '.gogradle/project_gopath').toPath()])
        ideaIntegration.generateXmls()
        assert new File(resource, '.idea/goLibraries.xml').text.contains('"file://$PROJECT_DIR$/.gogradle/project_gopath"')
    }
}
