package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.GolangConfigurationManager
import com.github.blindpirate.gogradle.core.GolangDependencyHandler
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

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
