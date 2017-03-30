package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
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
