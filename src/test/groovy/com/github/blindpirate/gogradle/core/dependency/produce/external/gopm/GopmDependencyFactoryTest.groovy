package com.github.blindpirate.gogradle.core.dependency.produce.external.gopm

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Spy

import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class GopmDependencyFactoryTest extends ExternalDependencyFactoryTest {
    @InjectMocks
    GopmDependencyFactory factory

    String dotGopmfile = '''
[target]
path = github.com/gogits/gogs

[deps]
github.com/a/b =
github.com/c/d
github.com/e/f = /path/to/my/project
github.com/g/h = d:\\projects\\xorm
golang.org/i/j = branch:master
golang.org/k/l = tag:v0.9.0
gopkg.in/redis.v2 = commit:e617904

[res]
include = public|scripts|templates'''

    @Test
    void 'parsing .gopmfile should success'() {
        // given
        IOUtils.write(resource, '.gopmfile', dotGopmfile)
        // when
        factory.produce(resource)

        // then
        verifyMapParsed([name: 'github.com/a/b'])
        verify(mapNotationParser).parse(eq([name: 'github.com/c/d']))
        verify(mapNotationParser).parse(eq([name: 'github.com/e/f', dir: '/path/to/my/project']))
        verify(mapNotationParser).parse(eq([name: 'github.com/g/h', dir: /d:\projects\xorm/]))
        verify(mapNotationParser).parse(eq([name: 'golang.org/i/j', branch: 'master']))
        verify(mapNotationParser).parse(eq([name: 'golang.org/k/l', tag: 'v0.9.0']))
        verify(mapNotationParser).parse(eq([name: 'gopkg.in/redis.v2', commit: 'e617904']))
    }

}
