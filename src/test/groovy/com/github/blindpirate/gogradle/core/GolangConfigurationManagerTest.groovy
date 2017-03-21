package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.GogradleRunner
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

@RunWith(GogradleRunner)
class GolangConfigurationManagerTest {
    @Mock
    Project project
    @Mock
    ConfigurationContainer configurationContainer

    @Test
    void 'getByName should be delegated to project'() {
        // given
        Mockito.when(project.getConfigurations()).thenReturn(configurationContainer)
        // when
        new GolangConfigurationManager(project).getByName('build')
        // then
        Mockito.verify(configurationContainer).getByName('build')
    }
}
