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

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import org.gradle.tooling.BuildController
import org.gradle.tooling.model.idea.IdeaProject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@WithResource('')
@RunWith(GogradleRunner)
class IdeaPluginIntegrationTest extends IntegrationTestSupport implements Serializable {
    static final long serialVersionUID = 1L

    @Override
    File getProjectRoot() {
        return resource
    }

    @Before
    void setUp() {
        String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='test'
}
"""
        writeBuildAndSettingsDotGradle(buildDotGradle)
    }

    @Test
    void 'getting IdeaProject should succeed'() {
        IdeaProject project = buildAction { BuildController controller ->
            controller.getModel(IdeaProject)
        }

        assert project.name == resource.name
    }
}
