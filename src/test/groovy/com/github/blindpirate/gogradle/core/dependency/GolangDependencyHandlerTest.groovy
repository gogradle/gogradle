package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.GolangConfigurationManager
import com.github.blindpirate.gogradle.core.GolangDependencyHandler
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser
import com.github.blindpirate.gogradle.task.GolangTaskContainer
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.query.ArtifactResolutionQuery
import org.gradle.api.internal.TaskInternal
import org.gradle.api.tasks.TaskContainer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class GolangDependencyHandlerTest {

    @Mock
    GolangConfigurationManager configurationManager
    @Mock
    GolangConfiguration configuration
    @Mock
    AbstractGolangDependency dependency
    @Mock
    NotationParser notationParser
    @Mock
    GolangDependencySet dependencies

    GolangDependencyHandler handler

    @Before
    void setUp() {
        handler = new GolangDependencyHandler(configurationManager, notationParser)
        when(configurationManager.getByName('build')).thenReturn(configuration)
        when(notationParser.parse('notation')).thenReturn(dependency)
        when(configuration.getDependencies()).thenReturn(dependencies)
    }

    @Test(expected = MissingMethodException)
    void 'exception should be thrown if no configuration found'() {
        handler.unexistent('')
    }

    @Test
    void 'adding configuration should succeed'() {
        handler.add('build', 'notation')
        verify(dependencies).add(dependency)
    }

    @Test
    void 'creating dependency should succeed'() {
        assert handler.create('notation', null).is(dependency)
    }
}
