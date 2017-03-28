package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangRepositoryHandler
import com.github.blindpirate.gogradle.core.StandardGolangPackage
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedPackageNotationDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency
import com.github.blindpirate.gogradle.vcs.Mercurial
import com.github.blindpirate.gogradle.vcs.VcsType
import com.github.blindpirate.gogradle.vcs.git.GolangRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock

import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.ArgumentMatchers.anyString
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
    GitMercurialNotationDependency dependency
    @Mock
    GolangRepositoryHandler repositoryHandler
    @Captor
    ArgumentCaptor captor

    @Before
    void setUp() {
        parser = new DefaultMapNotationParser(dirMapNotationParser, vendorMapNotationParser, packagePathResolver, repositoryHandler)
        when(repositoryHandler.findMatchedRepository(anyString())).thenReturn(GolangRepository.EMPTY_INSTANCE)
        when(vcsMapNotationParser.parse(anyMap())).thenReturn(dependency)
        when(dependency.getName()).thenReturn('name')
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
        Map notation = [name: 'path', vcs: '']
        VcsGolangPackage vcsPackage = VcsGolangPackage.builder()
                .withPath('path')
                .withRootPath('path')
                .withVcsType(VcsType.GIT)
                .withUrl('url')
                .build()
        when(packagePathResolver.produce('path')).thenReturn(of(vcsPackage))
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse([name: 'path', 'package': vcsPackage, vcs: ''])
    }

    @Test
    @WithMockInjector
    void 'urls in notation should be substituted'() {
        // given
        Map notation = [name: 'path', vcs: '']
        VcsGolangPackage vcsPackage = VcsGolangPackage.builder()
                .withPath('path')
                .withRootPath('path')
                .withVcsType(VcsType.GIT)
                .withUrls(['oldUrl1', 'oldUrl2'])
                .build()
        when(packagePathResolver.produce('path')).thenReturn(of(vcsPackage))
        when(repositoryHandler.findMatchedRepository(anyString())).thenReturn(new GolangRepository() {
            @Override
            String substitute(String name, String url) {
                return url.replace('old', 'new')
            }
        })
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse([name: 'path', 'package': vcsPackage, vcs: ''])
        verify(dependency).setUrls(['newUrl1', 'newUrl2'])
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
                .withUrl('url')
                .build()
        when(packagePathResolver.produce('root/path')).thenReturn(of(vcsPackage))
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse([name: 'root', 'package': vcsPackage])
    }

    @Test
    @WithMockInjector
    void 'unrecognized with url should be parsed successfully'() {
        // given
        Map notation = [name: 'unrecognized', url: 'url']
        when(packagePathResolver.produce('unrecognized')).thenReturn(of(UnrecognizedGolangPackage.of('unrecognized')))
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)
        // when
        parser.parse(notation)
        // then
        VcsGolangPackage pkg = VcsGolangPackage.builder()
                .withPath('unrecognized')
                .withRootPath('unrecognized')
                .withUrl('url')
                .withVcsType(VcsType.GIT)
                .build()

        verify(vcsMapNotationParser).parse(captor.capture())
        assertCaptorValue(captor.value, [name: 'unrecognized', url: 'url', vcs: 'git', 'package': pkg])
    }

    void assertCaptorValue(Map actual, Map expected) {
        assert actual.name == expected.name
        assert actual.url == expected.url
        assert actual.vcs == expected.vcs

        def actualPkg = actual.'package'
        def expectedPkg = expected.'package'
        assert actualPkg.path == expectedPkg.path
        assert actualPkg.rootPath == expectedPkg.rootPath
        assert actualPkg.urls == expectedPkg.urls
        assert actualPkg.vcsType == expectedPkg.vcsType
    }

    @Test
    @WithMockInjector
    void 'unrecognized package with url and vcs should be parsed successfully'() {
        // given
        Map notation = [name: 'unrecognized', url: 'url', vcs: 'hg']
        when(packagePathResolver.produce('unrecognized')).thenReturn(of(UnrecognizedGolangPackage.of('unrecognized')))
        MockUtils.mockVcsService(MapNotationParser, Mercurial, vcsMapNotationParser)
        // when
        parser.parse(notation)
        // then
        VcsGolangPackage pkg = VcsGolangPackage.builder()
                .withPath('unrecognized')
                .withRootPath('unrecognized')
                .withUrl('url')
                .withVcsType(VcsType.MERCURIAL)
                .build()

        verify(vcsMapNotationParser).parse(captor.capture())
        assertCaptorValue(captor.value, [name: 'unrecognized', url: 'url', vcs: 'hg', 'package': pkg])
    }

    @Test
    @WithMockInjector
    void 'unrecognized package with url substitution should be parsed successfully'() {
        // given
        Map notation = [name: 'unrecognized']
        when(packagePathResolver.produce('unrecognized')).thenReturn(of(UnrecognizedGolangPackage.of('unrecognized')))
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)
        when(repositoryHandler.findMatchedRepository(anyString())).thenReturn(new GolangRepository() {
            @Override
            String substitute(String name, String url) {
                return 'url'
            }
        })
        // when
        parser.parse(notation)
        // then
        VcsGolangPackage pkg = VcsGolangPackage.builder()
                .withPath('unrecognized')
                .withRootPath('unrecognized')
                .withUrl('url')
                .withVcsType(VcsType.GIT)
                .build()

        verify(vcsMapNotationParser).parse(captor.capture())
        assertCaptorValue(captor.value, [name: 'unrecognized', vcs: 'git', 'package': pkg])
    }

    @Test
    @WithMockInjector
    void 'url in unrecognized package will not be substituted'() {
        // given
        Map notation = [name: 'unrecognized', url: 'url1']
        when(packagePathResolver.produce('unrecognized')).thenReturn(of(UnrecognizedGolangPackage.of('unrecognized')))
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)
        when(repositoryHandler.findMatchedRepository(anyString())).thenReturn(new GolangRepository() {
            @Override
            String substitute(String name, String url) {
                return 'url2'
            }
        })
        // when
        parser.parse(notation)
        // then
        VcsGolangPackage pkg = VcsGolangPackage.builder()
                .withPath('unrecognized')
                .withRootPath('unrecognized')
                .withUrl('url1')
                .withVcsType(VcsType.GIT)
                .build()

        verify(vcsMapNotationParser).parse(captor.capture())
        assertCaptorValue(captor.value, [name: 'unrecognized', url: 'url1', vcs: 'git', 'package': pkg])
    }

    @Test
    void 'unrecognized notation dependency should be returned if unrecognized and no url specified'() {
        // given
        when(packagePathResolver.produce('unrecognized')).thenReturn(of(UnrecognizedGolangPackage.of('unrecognized')))
        // when
        def result = parser.parse([name: 'unrecognized'])
        // then
        assert result instanceof UnrecognizedPackageNotationDependency
        assert result.name == 'unrecognized'
    }

    @Test(expected = IllegalStateException)
    void 'notation with mismatched vcs should result in an exception'() {
        // given
        Map notation = [name: 'github.com/user/package', vcs: 'svn']
        VcsGolangPackage vcsPackage = MockUtils.mockVcsPackage()
        when(packagePathResolver.produce('github.com/user/package')).thenReturn(of(vcsPackage))
        // when
        parser.parse(notation)
    }

    @Test(expected = DependencyResolutionException)
    void 'only VcsGolangPackage and UnrecognizedGolangPackage can be parsed'() {
        // given
        when(packagePathResolver.produce('fmt')).thenReturn(of(StandardGolangPackage.of('fmt')))
        parser.parse([name: 'fmt'])
    }
}
