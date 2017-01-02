package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(GogradleRunner)
class SourceCodeAnalysisTest extends GogradleModuleSupport {

    SourceCodeDependencyFactory factory

    File resource

    @Mock
    ResolvedDependency resolvedDependency

    @Before
    void setUp() {
        factory = injector.getInstance(SourceCodeDependencyFactory)
    }

    @WithResource('golang-example-master.zip')
    @Test
    void 'imports should be parsed correctly'() {
        // when
        GolangDependencySet result = factory.produce(resource)

        // then
        def expectation = ['golang.org/x/tools', 'github.com/golang/example'] as Set
        assert result.size() == expectation.size()
        result.each {
            assert expectation.contains(it.name)
        }

    }
}
