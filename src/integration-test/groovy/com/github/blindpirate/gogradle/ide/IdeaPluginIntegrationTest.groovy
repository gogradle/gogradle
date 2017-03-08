package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.gradle.tooling.BuildController
import org.gradle.tooling.model.idea.IdeaProject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@WithResource('')
@RunWith(GogradleRunner)
class IdeaPluginIntegrationTest extends IntegrationTestSupport implements Serializable {
    static final long serialVersionUID = 1L

    File resource

    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='test'
}
"""

    @Override
    File getProjectRoot() {
        return resource
    }

    @Before
    void setUp() {
        baseSetUp()
        IOUtils.write(resource, 'build.gradle',
                StringUtils.render(buildDotGradle, [classpath: getClasspath(), goBinPath: getGoBinPath()]))
    }

    @Test
    void 'getting IdeaProject should succeed'() {
        IdeaProject project = buildAction { BuildController controller ->
            controller.getModel(IdeaProject)
        }

        assert project.name == resource.name
    }
}
