package com.github.blindpirate.gogradle.core.dependency.produce.external.glock

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class GlockDependencyFactoryTest extends ExternalDependencyFactoryTest {
    @InjectMocks
    GlockDependencyFactory factory

    @Test
    void 'package without GLOCKFILE should be rejected'() {
        assert !factory.produce(resource, 'build').isPresent()
    }

    String GLOCKFILE = '''
bitbucket.org/tebeka/selenium 02df1758050f
code.google.com/p/cascadia 4f03c71bc42b
code.google.com/p/go-uuid 7dda39b2e7d5
'''

    @Test
    void 'parsing GLOCKFILE should succeed'() {
        // given
        prepareGlockfile(GLOCKFILE)
        // when
        factory.produce(resource, 'build')
        // then
        verifyMapParsed([name: 'bitbucket.org/tebeka/selenium', version: '02df1758050f'])
        verifyMapParsed([name: 'code.google.com/p/cascadia', version: '4f03c71bc42b'])
        verifyMapParsed([name: 'code.google.com/p/go-uuid', version: '7dda39b2e7d5'])
    }

    String glockfileWithCmds = '''
cmd code.google.com/p/go.tools/cmd/godoc
cmd code.google.com/p/go.tools/cmd/goimports
cmd code.google.com/p/go.tools/cmd/vet
bitbucket.org/tebeka/selenium 02df1758050f
code.google.com/p/cascadia 4f03c71bc42b
code.google.com/p/go-uuid 7dda39b2e7d5
'''

    @Test
    void 'cmd lines should be ignored'() {
        // given
        prepareGlockfile(glockfileWithCmds)
        // when
        factory.produce(resource, 'build')
        // then
        verify(mapNotationParser, times(3)).parse(anyMap())
    }

    String glockfileWithCorruptedLine = '''
This is a corrupted line
'''

    @Test(expected = RuntimeException)
    void 'unrecognized line should cause an exception'() {
        // given
        prepareGlockfile(glockfileWithCorruptedLine)
        // then
        factory.produce(resource, 'build')
    }

    void prepareGlockfile(String s) {
        IOUtils.write(resource, "GLOCKFILE", s)
    }
}
