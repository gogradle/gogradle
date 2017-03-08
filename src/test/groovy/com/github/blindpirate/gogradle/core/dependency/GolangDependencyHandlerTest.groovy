package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
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

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GolangDependencyHandlerTest {

    @Mock
    ConfigurationContainer configurationContainer
    @Mock
    Configuration configuration
    @Mock
    AbstractGolangDependency dependency
    @Mock
    NotationParser notationParser
    @Mock
    DependencySet dependencies
    @Mock
    Project project

    GolangDependencyHandler handler

    @Before
    void setUp() {
        handler = new GolangDependencyHandler(configurationContainer, notationParser, project)
        when(configurationContainer.findByName('build')).thenReturn(configuration)
        when(notationParser.parse('notation')).thenReturn(dependency)
        when(configuration.getDependencies()).thenReturn(dependencies)
    }

    @Test
    void 'unsupported method should all throw UnsupportedException'() {
        ReflectionUtils.testUnsupportedMethods(handler, DependencyHandler, ['create', 'add', 'createArtifactResolutionQuery'])
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
        assert handler.create('notation').is(dependency)
    }

    @Test
    void 'hacking idea should succeed'() {
        // given
        TaskContainer taskContainer = mock(TaskContainer)
        TaskInternal task = mock(TaskInternal)
        when(project.getTasks()).thenReturn(taskContainer)
        when(taskContainer.getByName(GolangTaskContainer.IDEA_TASK_NAME)).thenReturn(task)
        // when
        ArtifactResolutionQuery query = handler.createArtifactResolutionQuery()
        // then
        assert query.forComponents([])
                .forComponents()
                .withArtifacts(null)
                .execute()
                .getComponents().isEmpty()
        assert query.forComponents([])
                .forComponents()
                .withArtifacts(null)
                .execute()
                .getResolvedComponents().isEmpty()
        verify(task).execute()
    }
}
