package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultMapNotationParserTest {

    DefaultMapNotationParser parser
    @Mock
    DirMapNotationParser dirMapNotationParser
    @Mock
    VendorMapNotationParser vendorMapNotationParser
    @Mock
    PackagePathResolver packagePathResolver
    @Mock
    MapNotationParser vcsMapNotationParser
    @Mock
    GolangDependency dependency

    @Before
    void setUp() {
        parser = new DefaultMapNotationParser(dirMapNotationParser, vendorMapNotationParser, packagePathResolver)
    }

    @Test(expected = Exception)
    void 'notation without name should be rejected'() {
        parser.parse([:])
    }

    @Test
    void 'notation with dir should be delegated to DirMapNotationParser'() {
        // given
        def notation = [name: 'path', dir: 'dir']
        // when
        parser.parse(notation)
        // then
        verify(dirMapNotationParser).parse(notation)
    }

    @Test
    void 'notation with vendorPath should be delegated to VendorMapNotationParser'() {
        // given
        def notation = [name: 'path', vendorPath: '/vendor/github.com/a/b']
        // when
        parser.parse(notation)
        // then
        verify(vendorMapNotationParser).parse(notation)
    }

    @Test
    @WithMockInjector
    void 'notation should be delegated to vcs parser'() {
        // given
        Map notation = [name: 'path']
        VcsGolangPackage vcsPackage = VcsGolangPackage.builder()
                .withPath('path')
                .withRootPath('path')
                .withVcsType(VcsType.GIT)
                .build()
        when(packagePathResolver.produce('path')).thenReturn(Optional.of(vcsPackage))
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse(eq([name: 'path', 'package': vcsPackage]))
    }

    @Test
    @WithMockInjector
    void 'sub package declaration should be normalized'() {
        // given
        Map notation = [name: 'root/path']
        VcsGolangPackage vcsPackage = VcsGolangPackage.builder()
                .withPath('root/path')
                .withRootPath('root')
                .withVcsType(VcsType.GIT)
                .build()
        when(packagePathResolver.produce('root/path')).thenReturn(Optional.of(vcsPackage))
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse(eq([name: 'root', 'package': vcsPackage]))
    }

}
