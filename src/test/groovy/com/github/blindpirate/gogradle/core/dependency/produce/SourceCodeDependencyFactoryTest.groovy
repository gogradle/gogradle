/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.*
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedNotationDependency
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static com.github.blindpirate.gogradle.core.GolangRepository.newOriginalRepository
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class SourceCodeDependencyFactoryTest {
    @Mock
    PackagePathResolver packagePathResolver
    @Mock
    NotationParser notationParser
    @Mock
    BuildConstraintManager buildConstraintManager
    @Mock
    GogradleRootProject gogradleRootProject

    GoImportExtractor extractor

    SourceCodeDependencyFactory factory

    File resource

    @Mock
    ResolvedDependency resolvedDependency

    @Before
    void setUp() {
        extractor = new GoImportExtractor(buildConstraintManager)
        factory = new SourceCodeDependencyFactory(packagePathResolver, notationParser, extractor, gogradleRootProject)
        when(resolvedDependency.getName()).thenReturn('root/package')
        when(resolvedDependency.getSubpackages()).thenReturn(['...'] as Set)
        when(buildConstraintManager.getAllConstraints()).thenReturn([] as Set)
        when(gogradleRootProject.getName()).thenReturn('github.com/my/package')
        when(packagePathResolver.produce(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                String path = invocation.getArgument(0)
                if (path.startsWith('github.com/incomplete')) {
                    return Optional.of(IncompleteGolangPackage.of('github.com/incomplete'))
                } else if (path.startsWith('github.com')) {
                    String rootPath = path.split('/')[0..2].join('/')
                    GolangPackage ret = VcsGolangPackage.builder()
                            .withPath(path)
                            .withRootPath(rootPath)
                            .withRepository(newOriginalRepository(VcsType.GIT, ["https://${rootPath}.git"]))
                            .build()
                    return Optional.of(ret)
                } else {
                    GolangPackage standardPackage = StandardGolangPackage.of(path)
                    return Optional.of(standardPackage)
                }
            }
        })

        when(notationParser.parse(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return DependencyUtils.mockWithName(GolangDependency, invocation.getArgument(0))
            }
        })
    }

    @Test
    void 'empty dependency set should be produced when no go code exists'() {
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
        assert factory.produce(resolvedDependency, resource, 'test').isEmpty()
    }

    @Test
    void 'self dependency and root project should be ignored'() {
        // given
        IOUtils.write(resource, 'main.go', '''
package main
import (
    "root/package/a"
    "root/package/a/b"
    "root/package/a/b/c"
    "github.com/my/package"
)
func Whatever(){}
''')
        // then
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
        verify(packagePathResolver, times(0)).produce(anyString())
    }

    @Test
    void 'standard relative blank path should be ignored'() {
        // given
        IOUtils.write(resource, 'main.go', '''
package main
import (
    "fmt"
    "go/types"
    "log"
    "os"
    "strings"
    "unicode"
    "unicode/utf8"
    "./another"
    `../another`
    _ "./another"
    A "../another"
    " "
    '\t\t'
    )
func main(){}
''')
        // then
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
    }

    @Test
    void 'import paths should be processed correctly'() {
        // given
        IOUtils.write(resource, 'main.go', mainDotGo)
        // when
        GolangDependencySet result = factory.produce(resolvedDependency, resource, 'build')
        // then
        assert result.size() == 1
        assert result.first().name == 'github.com/a/b'
    }

    @Test
    void 'import paths should be processed correctly in test'() {
        // given
        IOUtils.write(resource, 'main_test.go', mainDotGo)
        // when
        GolangDependencySet result = factory.produce(resolvedDependency, resource, 'test')
        // then
        assert result.size() == 1
        assert result.first().name == 'github.com/a/b'
    }

    @Test
    void 'directory should be searched recursively'() {
        // given
        IOUtils.write(resource, 'sub/sub/main.go', mainDotGo)
        IOUtils.write(resource, 'sub/sub/main_test.go', mainDotGo)
        IOUtils.write(resource, 'sub/sub/garbage', 'This is useless')
        // when
        GolangDependencySet buildResult = factory.produce(resolvedDependency, resource, 'build')
        GolangDependencySet testResult = factory.produce(resolvedDependency, resource, 'test')
        // then
        assert buildResult.size() == 1
        assert buildResult.first().name == 'github.com/a/b'
        assert testResult.size() == 1
        assert testResult.first().name == 'github.com/a/b'
    }

    @Test
    void 'searched with subpackages should succeed'() {
        /*
        a
        |--b
        |  |-- c
        |  |   \- c.go
        |  |
        |  \-- d
        |      \- d.go
        \--e
           \-- f
               \-- f.go
         */
        // given
        IOUtils.write(resource, 'a/b/c/c.go', '''
package main
import (
    "github.com/my/c"
)
func Whatever(){}
''')
        IOUtils.write(resource, 'a/b/d/d.go', '''
package main
import (
    "github.com/my/d"
)
func Whatever(){}
''')
        IOUtils.write(resource, 'a/e/f/f.go', '''
package main
import (
    "github.com/my/f"
)
func Whatever(){}
''')
        when(resolvedDependency.getSubpackages()).thenReturn(['a/b/d', 'a/e'] as Set)
        // when
        GolangDependencySet buildResult = factory.produce(resolvedDependency, resource, 'build')
        // then
        assert buildResult.size() == 2
        assert buildResult.any { it.name == 'github.com/my/d' }
        assert buildResult.any { it.name == 'github.com/my/f' }
    }

    @Test
    void 'files starting with `_` and `dot` should be ignored'() {
        // given
        IOUtils.write(resource, '_.go', mainDotGo)
        IOUtils.write(resource, '.should_be_ignored.go', mainDotGo)
        // then
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
        assert factory.produce(resolvedDependency, resource, 'test').isEmpty()
    }

    @Test
    void 'directories starting with `_` and `dot` should be ignored'() {
        // given
        IOUtils.write(resource, '_/main.go', mainDotGo)
        IOUtils.write(resource, '.should_be_ignored/main.go', mainDotGo)
        // then
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
        assert factory.produce(resolvedDependency, resource, 'test').isEmpty()
    }

    @Test
    void 'files ending with `_test` should be ignored in build dependencies'() {
        // given
        IOUtils.write(resource, 'a_test.go', mainDotGo)
        IOUtils.write(resource, '_test.go', mainDotGo)
        // when
        GolangDependencySet result = factory.produce(resolvedDependency, resource, 'build')
        // then
        assert result.isEmpty()
    }

    @Test
    void 'files not ending with `_test` should be ignored in test dependencies'() {
        // given
        IOUtils.write(resource, 'main1.go', mainDotGo)
        IOUtils.write(resource, 'main2.go', mainDotGo)
        // when
        GolangDependencySet result = factory.produce(resolvedDependency, resource, 'test')
        // then
        assert result.isEmpty()
    }

    @Test
    void 'files in vendor directory should be ignored'() {
        // given
        File vendorDir = new File(resource, 'vendor')
        IOUtils.forceMkdir(vendorDir)
        IOUtils.write(vendorDir, "main.go", mainDotGo)
        IOUtils.write(vendorDir, "main_test.go", mainDotGo)
        // then
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
        assert factory.produce(resolvedDependency, resource, 'test').isEmpty()
    }

    @Test
    void 'files in testdata directory should be ignored'() {
        // given
        File vendorDir = new File(resource, 'testdata')
        IOUtils.forceMkdir(vendorDir)
        IOUtils.write(vendorDir, "main.go", mainDotGo)
        IOUtils.write(vendorDir, "main_test.go", mainDotGo)
        // then
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
        assert factory.produce(resolvedDependency, resource, 'test').isEmpty()
    }

    @Test
    void 'self dependency should be excluded'() {
        // given
        IOUtils.write(resource, 'main.go', mainDotGo)
        IOUtils.write(resource, 'main_test.go', mainDotGo)
        when(resolvedDependency.getName()).thenReturn('github.com/a/b')
        // then
        assert factory.produce(resolvedDependency, resource, 'build').isEmpty()
        assert factory.produce(resolvedDependency, resource, 'test').isEmpty()
    }

    @Test
    void 'exception should be thrown if import package cannot be recognized'() {
        // given
        IOUtils.write(resource, 'main.go', mainDotGo)
        Mockito.reset(notationParser, packagePathResolver)
        ['github.com/a/b', 'github.com/a/b/c', 'github.com/a/b/c/d'].each {
            when(packagePathResolver.produce(it)).thenReturn(Optional.of(UnrecognizedGolangPackage.of(it)))
            when(notationParser.parse(it)).thenReturn(UnrecognizedNotationDependency.of(UnrecognizedGolangPackage.of(it)))
        }
        // then
        def result = factory.produce(resolvedDependency, resource, 'build')
        assert result.size() == 3
        assert result.collect {
            it.name
        }.minus(['github.com/a/b', 'github.com/a/b/c', 'github.com/a/b/c/d']).isEmpty()
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if import package is incomplete'() {
        IOUtils.write(resource, 'main.go', '''
package main
import (
        "github.com/incomplete"
    )
func main(){}
''')
        factory.produce(resolvedDependency, resource, 'build')
    }

    String mainDotGo = '''
package main
import (
    "github.com/a/b"
    "github.com/a/b/c"
    "github.com/a/b/c/d"
)
func main(){}
'''


}
