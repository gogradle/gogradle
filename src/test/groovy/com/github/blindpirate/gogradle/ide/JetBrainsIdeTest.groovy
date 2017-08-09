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
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class JetBrainsIdeTest {
    @Mock
    GoBinaryManager manager
    @Mock
    Project project
    @Mock
    BuildManager buildManager

    File resource

    JetBrainsIdeIntegration JetBrainsIdeIntegration

    GoglandIntegration goglandIntegration

    @Before
    void setUp() {
        JetBrainsIdeIntegration = new JetBrainsIdeIntegration(manager, project, buildManager)
        goglandIntegration = new GoglandIntegration(manager, project, buildManager)


        when(manager.getBinaryPath()).thenReturn(new File(resource, 'go/bin/go').toPath())
        when(manager.getGoroot()).thenReturn(new File(resource, 'go').toPath())
        when(manager.getGoVersion()).thenReturn('1.7.1')


        when(project.getProjectDir()).thenReturn(resource)

        when(buildManager.getGopath()).thenReturn(toUnixString(resource) + File.pathSeparator + toUnixString(resource))
        when(buildManager.getGopaths()).thenReturn([resource.toPath(), resource.toPath()])

        when(project.getName()).thenReturn('MyAwesomeProject')
    }

    @Test
    void 'xmls should be generated correctly'() {
        JetBrainsIdeIntegration.generateXmls()

        assert new File(resource, '.idea/goLibraries.xml').text.count("\"file://${toUnixString(resource)}\"") == 2

        String moduleIml = new File(resource, '.idea/MyAwesomeProject.iml').text
        assert moduleIml.contains('WEB_MODULE')
        assert moduleIml.contains('Go SDK')

        String goSdkXml = new File(resource, '.idea/libraries/Go_SDK.xml').text
        assert goSdkXml.contains("file://${toUnixString(new File(resource, 'go/src'))}")

        String modulesXml = new File(resource, '.idea/modules.xml').text
        assert modulesXml.contains('.idea/MyAwesomeProject.iml')
    }

    @Test
    void 'xmls should be generated correctly for gogland'() {
        goglandIntegration.generateXmls()

        assert !new File(resource, '.idea/goLibraries.xml').exists()
        assert !new File(resource, '.idea/libraries/Go_SDK.xml').exists()
        assert new File(resource, '.idea/MyAwesomeProject.iml').exists()

        assert new File(resource, '.idea/misc.xml').text.count("\"file://${toUnixString(resource)}\"") == 2

        assert new File(resource, '.idea/modules.xml').text.contains('.idea/MyAwesomeProject.iml')
    }
}
