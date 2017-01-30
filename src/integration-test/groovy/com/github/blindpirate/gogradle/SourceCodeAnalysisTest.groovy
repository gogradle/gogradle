package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.GogradleModuleSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.google.inject.Inject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import static com.github.blindpirate.gogradle.build.Configuration.BUILD

@RunWith(GogradleRunner)
class SourceCodeAnalysisTest extends GogradleModuleSupport {

    @Inject
    SourceCodeDependencyFactory factory

    File resource

    @Mock
    ResolvedDependency resolvedDependency

    @WithResource('golang-example-master.zip')
    @Test
    @AccessWeb
    void 'imports should be parsed correctly'() {
        // given
        Mockito.when(resolvedDependency.getName()).thenReturn("name")
        // when
        GolangDependencySet result = factory.produce(resolvedDependency, resource, BUILD)

        // then
        def expectation = ['golang.org/x/tools', 'github.com/golang/example'] as Set
        assert result.collect { it.name } as Set == expectation
    }
}
