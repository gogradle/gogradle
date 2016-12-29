package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.BuildConstraintManager
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser
import com.github.blindpirate.gogradle.core.dependency.produce.GoImportExtractor
import com.github.blindpirate.gogradle.core.pack.PackageInfo
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class SourceCodeDependencyFactoryTest {
    @Mock
    PackagePathResolver packagePathResolver
    @Mock
    NotationParser notationParser;
    @Mock
    BuildConstraintManager buildConstraintManager;

    GoImportExtractor extractor

    SourceCodeDependencyFactory factory

    File resource

    @Mock
    GolangPackageModule module
    @Mock
    GolangDependency dependency

    @Before
    void setUp() {
        extractor = new GoImportExtractor(buildConstraintManager)
        factory = new SourceCodeDependencyFactory(packagePathResolver, notationParser, extractor)
        when(buildConstraintManager.getCtx()).thenReturn([] as Set)
        when(module.getRootDir()).thenReturn(resource.toPath())
        when(notationParser.parse('github.com/a/b')).thenReturn(dependency)
        when(dependency.getName()).thenReturn('github.com/a/b')
        when(packagePathResolver.produce(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                String name = invocation.getArgument(0);
                if (name.startsWith('github.com')) {
                    PackageInfo ret = PackageInfo.builder().withPath(name).withRootPath('github.com/a/b').build()
                    return Optional.of(ret)
                } else {
                    PackageInfo standardPackage = PackageInfo.standardPackage(name);
                    return Optional.of(standardPackage)
                }
            }
        })
    }

    @Test
    void 'empty dependency set should be produced when no go code exists'() {
        // when
        GolangDependencySet result = factory.produce(module).get()
        // then
        assert result.isEmpty()
    }

    @Test
    void 'standard/relative/blank path should be ignored'() {
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
        // when
        GolangDependencySet result = factory.produce(module).get()
        assert result.isEmpty()
    }

    @Test
    void 'import paths should be processed correctly'() {
        // given
        IOUtils.write(resource, 'main.go', mainDotGo)
        // when
        GolangDependencySet result = factory.produce(module).get()
        // then
        assert result.size() == 1
        assert result.any { it.is(dependency) }
    }

    @Test
    void 'directory should be searched recursively'() {
        // given
        File sub = resource.toPath().resolve('sub').toFile()
        File subsub = sub.toPath().resolve('sub').toFile()
        IOUtils.forceMkdir(sub)
        IOUtils.forceMkdir(sub)
        IOUtils.write(subsub, 'main.go', mainDotGo)
        IOUtils.write(subsub, 'garbage', 'This is unused')
        // when
        GolangDependencySet result = factory.produce(module).get()
        // then
        assert result.size() == 1
        assert result.any { it.is(dependency) }
    }

    @Test
    void 'files starting with `_` and `dot` should be ignored'() {
        // given
        IOUtils.write(resource, '_.go', mainDotGo)
        IOUtils.write(resource, '.should_be_ignored.go', mainDotGo)
        // when
        GolangDependencySet result = factory.produce(module).get()
        // then
        assert result.isEmpty()
    }

    @Test
    void 'files ending with `_test` should be ignored'() {
        // given
        IOUtils.write(resource, 'a_test.go', mainDotGo)
        IOUtils.write(resource, '_test.go', mainDotGo)
        // when
        GolangDependencySet result = factory.produce(module).get()
        // then
        assert result.isEmpty()
    }

    @Test
    void 'files in vendor directory should be ignored'() {
        // given
        File vendorDir = resource.toPath().resolve('vendor').toFile()
        IOUtils.forceMkdir(vendorDir)
        IOUtils.write(vendorDir, "main.go", mainDotGo)
        // then
        assert factory.produce(module).get().isEmpty()
    }

    @Test
    void 'files in testdata directory should be ignored'() {
        // given
        File vendorDir = resource.toPath().resolve('testdata').toFile()
        IOUtils.forceMkdir(vendorDir)
        IOUtils.write(vendorDir, "main.go", mainDotGo)
        // then
        assert factory.produce(module).get().isEmpty()
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
