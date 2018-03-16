package com.github.blindpirate.gogradle.core.dependency.produce.external

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

import java.nio.file.Paths

import static org.mockito.Mockito.verifyNoMoreInteractions
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class ExternalDependencyFactoryTest extends AbstractExternalDependencyFactoryTest {
    @InjectMocks
    TestForExternalDependencyFactory factory = new TestForExternalDependencyFactory()

    List dependencies

    @Test
    void 'standard package should be excluded'() {
        // given
        dependencies = [[name: 'plugin'], [name: 'github.com/another/project']]
        when(standardPackagePathResolver.isStandardPackage(Paths.get('plugin'))).thenReturn(true)

        // when
        factory.produce(parentDependency, resource, 'build')

        // then
        verifyMapParsed([name: 'github.com/another/project'])
        verifyNoMoreInteractions(mapNotationParser)
    }

    @Test
    void 'parent package and gogradle root should be excluded'() {
        // given
        dependencies = [[name: 'github.com/my/project_with_same_prefix'],
                        [name: 'github.com/target/project'],
                        [name: 'github.com/target/project/sub'],
                        [name: 'github.com/my/project'],
                        [name: 'github.com/my/project/sub']

        ]

        // when
        factory.produce(parentDependency, resource, 'build')

        // then
        verifyMapParsed(name: 'github.com/my/project_with_same_prefix')
        verifyNoMoreInteractions(mapNotationParser)
    }

    class TestForExternalDependencyFactory extends ExternalDependencyFactory {
        @Override
        String identityFileName() {
            return "whatever"
        }

        @Override
        protected List<Map<String, Object>> adapt(File file) {
            return dependencies
        }
    }
}
