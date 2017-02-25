package com.github.blindpirate.gogradle.ide

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

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

    File resource

    IdeaIntegration ideaIntegration

    @Before
    void setUp() {
        ideaIntegration = new IdeaIntegration(manager, project, hacker)
        when(project.getRootDir()).thenReturn(resource)
        when(manager.getBinaryPath()).thenReturn(new File(resource, 'go/bin/go').toPath())
        when(manager.getGoroot()).thenReturn(new File(resource, 'go').toPath())
        when(manager.getGoVersion()).thenReturn('1.7.1')

        when(project.getName()).thenReturn('MyAwesomeProject')
    }

    @Test
    void 'idea xmls should be generated correctly'() {
        ideaIntegration.generateIdeaXmls()

        verify(hacker).ensureSpecificSdkExist('1.7.1', new File(resource, 'go').toPath())

        assert new File(resource, '.idea/goLibraries.xml').exists()

        String moduleIml = IOUtils.toString(new File(resource, '.idea/modules/MyAwesomeProject.iml'))
        assert moduleIml.contains('GO_MODULE')
        assert moduleIml.contains('Go 1.7.1')
        assert moduleIml.contains('Go SDK')

        String modulesXml = IOUtils.toString(new File(resource, '.idea/modules.xml'))
        assert modulesXml.contains('.idea/modules/MyAwesomeProject.iml')
    }
}
