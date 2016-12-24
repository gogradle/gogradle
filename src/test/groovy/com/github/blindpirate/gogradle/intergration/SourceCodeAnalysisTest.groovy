package com.github.blindpirate.gogradle.intergration

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.resolve.SourceCodeDependencyFactory
import com.github.blindpirate.gogradle.core.infrastructure.GogradleModuleSupport
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class SourceCodeAnalysisTest extends GogradleModuleSupport {

    SourceCodeDependencyFactory factory

    File resource

    @Mock
    GolangPackageModule module

    @Before
    void setUp() {
        factory = injector.getInstance(SourceCodeDependencyFactory)
    }

    @WithResource('golang-example-master.zip')
    @Test
    void 'imports should be parsed correctly'() {
        // given
        when(module.getRootDir()).thenReturn(resource.toPath())
        // when
        GolangDependencySet result = factory.produce(module).get()

        // then
        def expectation = ['golang.org/x/tools', 'github.com/golang/example'] as Set
        assert result.size() == expectation.size()
        result.each {
            assert expectation.contains(it.name)
        }

    }
}
