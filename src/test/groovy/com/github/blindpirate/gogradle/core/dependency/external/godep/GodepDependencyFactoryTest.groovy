package com.github.blindpirate.gogradle.core.dependency.external.godep

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GodepDependencyFactoryTest extends ExternalDependencyFactoryTest {
    @InjectMocks
    GodepDependencyFactory godepDependencyFactory

    @Test
    void 'package with Godeps/Godeps.json should be rejected'() {
        // then:
        assert !godepDependencyFactory.produce(module).isPresent()
    }

    String GodepsDotJson = '''
            {
                "ImportPath": "github.com/tools/godep",
                "GoVersion": "go1.7",
                "GodepVersion": "v74",
                "Deps": [
                    {
                        "ImportPath": "github.com/kr/fs",
                        "Rev": "2788f0dbd16903de03cb8186e5c7d97b69ad387b"
                    },
                    {
                        "ImportPath": "github.com/kr/pretty",
                        "Comment": "go.weekly.2011-12-22-24-gf31442d",
                        "Rev": "f31442d60e51465c69811e2107ae978868dbea5c"
                    }
            ]
            }
            '''

    @Test
    void 'package with Godeps/Godeps.json should be analyzed properly'() {
        // given:
        prepareGodepsDotJson(GodepsDotJson)

        // when:
        godepDependencyFactory.produce(module)
        // then:
        verifyMapParsed([name: "github.com/kr/fs", revision: '2788f0dbd16903de03cb8186e5c7d97b69ad387b'])
        verifyMapParsed([name    : "github.com/kr/pretty",
                         revision: 'f31442d60e51465c69811e2107ae978868dbea5c'])
    }

    @Test(expected = RuntimeException)
    void 'corrupted Godeps.json should result in an exception'() {
        // given
        prepareGodepsDotJson('This is a corrupted Godeps.json')
        // then
        godepDependencyFactory.produce(module)
    }

    @Test(expected = RuntimeException)
    void 'blank ImportPath or Rev should cause an exception'() {
        // given
        prepareGodepsDotJson('''
{
    "Deps":[{"ImportPath":"  ","Rev":"a"},{"ImportPath":"a"}]
}
''')
        // then
        godepDependencyFactory.produce(module)
    }

    void prepareGodepsDotJson(String GodepsDotJson) {
        IOUtils.write(resource, 'Godeps/Godeps.json', GodepsDotJson);
        when(module.getRootDir()).thenReturn(resource.toPath())
    }

    String GodepsDotJsonWithExtraAndMissingProperties = '''
            {
                "extraProperties":[1,"a",{"c":null}],
                "ImportPath": "github.com/tools/godep",
                "Deps": [{"a":1,"ImportPath":"a","Rev":"b"}]
            }
            '''

    @Test
    void 'extra properties in Godeps/Godeps.json should be ignored'() {
        // given
        prepareGodepsDotJson(GodepsDotJsonWithExtraAndMissingProperties)
        // when
        godepDependencyFactory.produce(module)
        // then
        verifyMapParsed([name: 'a', revision: 'b'])
    }

}
