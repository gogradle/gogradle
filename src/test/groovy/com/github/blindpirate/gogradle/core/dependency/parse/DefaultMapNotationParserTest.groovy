package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangRepositoryHandler
import com.github.blindpirate.gogradle.core.StandardGolangPackage
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedPackageNotationDependency
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependencyTest
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency
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
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.ArgumentMatchers.anyMapOf
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithMockInjector
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
    GitMercurialNotationDependency gitMercurialNotationDependency
    @Mock
    LocalDirectoryDependency localDirectoryDependency
    @Mock
    VendorNotationDependency vendorNotationDependency
    @Mock
    UnrecognizedPackageNotationDependency unrecognizedPackageNotationDependency
    @Mock
    GolangRepositoryHandler repositoryHandler
    @Captor
    ArgumentCaptor captor

    @Before
    void setUp() {
        parser = new DefaultMapNotationParser(dirMapNotationParser, vendorMapNotationParser, packagePathResolver, repositoryHandler)
        when(repositoryHandler.findMatchedRepository(anyString())).thenReturn(GolangRepository.EMPTY_INSTANCE)

        when(localDirectoryDependency.getName()).thenReturn('localDirectory')
        when(unrecognizedPackageNotationDependency.getName()).thenReturn('unrecognized')
        when(vendorNotationDependency.getName()).thenReturn('vendor')
        when(gitMercurialNotationDependency.getName()).thenReturn('gitMercurial')

        when(dirMapNotationParser.parse(anyMap())).thenReturn(localDirectoryDependency)
        when(vendorMapNotationParser.parse(anyMap())).thenReturn(vendorNotationDependency)
        when(vcsMapNotationParser.parse(anyMap())).thenReturn(gitMercurialNotationDependency)

        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)

        when(packagePathResolver.produce(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                String path = invocation.getArgument(0)
                if (path.startsWith('unrecognized')) {
                    return of(UnrecognizedGolangPackage.of(path))
                } else {
                    return of(VcsGolangPackage.builder()
                            .withPath(path)
                            .withRootPath(path)
                            .withVcsType(VcsType.GIT)
                            .withUrl('url')
                            .build())
                }
            }
        })
    }

    @Test(expected = IllegalStateException)
    void 'notation without name should be rejected'() {
        parser.parse([:])
    }

    @Test
    void 'unrecognized package with dir should be delegated to DirMapNotationParser'() {
        // when
        when(localDirectoryDependency.getName()).thenReturn('unrecognized')
        NotationDependency result = parser.parse([name: 'unrecognized', dir: 'dir'])
        NotationDependency subResult = parser.parse([name: 'unrecognized/sub'])
        // then
        assert result.is(localDirectoryDependency)
        assert subResult.is(localDirectoryDependency)
        verify(dirMapNotationParser).parse(captor.capture())
        assert captor.value == [name: 'unrecognized', dir: 'dir']
    }

    @Test
    void 'unrecognized dependency should be produced after its parent is produced'() {
        // when
        when(localDirectoryDependency.getName()).thenReturn('unrecognized/sub')
        NotationDependency subResult = parser.parse([name: 'unrecognized/sub', dir: 'dir'])
        NotationDependency result = parser.parse([name: 'unrecognized'])
        // then
        assert subResult.is(localDirectoryDependency)
        assert result instanceof UnrecognizedPackageNotationDependency
        assert result.name == 'unrecognized'
    }

    @Test
    void 'unrecognized dependency should be parsed successfully'() {
        // when
        NotationDependency result = parser.parse([name: 'unrecognized'])
        NotationDependency subResult = parser.parse([name: 'unrecognized/sub'])
        // then
        assert result instanceof UnrecognizedPackageNotationDependency
        assert subResult instanceof UnrecognizedPackageNotationDependency
        assert result.name == 'unrecognized'
        assert subResult.name == 'unrecognized/sub'
    }

    @Test
    void 'unrecognized dependency should be parsed successfully in reverse order'() {
        // when
        NotationDependency subResult = parser.parse([name: 'unrecognized/sub'])
        NotationDependency result = parser.parse([name: 'unrecognized'])
        // then
        assert result instanceof UnrecognizedPackageNotationDependency
        assert subResult instanceof UnrecognizedPackageNotationDependency
        assert result.name == 'unrecognized'
        assert subResult.name == 'unrecognized/sub'
    }

    @Test
    void 'normal package in dir should be parsed successfully'() {
        // given
        when(packagePathResolver.produce('path/sub')).thenReturn(of(
                VcsGolangPackage.builder()
                        .withPath('path/sub')
                        .withRootPath('path')
                        .withUrl('url')
                        .withVcsType(VcsType.GIT)
                        .build()
        ))
        when(packagePathResolver.produce('path')).thenReturn(of(
                VcsGolangPackage.builder()
                        .withPath('path')
                        .withRootPath('path')
                        .withUrl('url')
                        .withVcsType(VcsType.GIT)
                        .build()
        ))
        // when
        NotationDependency result = parser.parse([name: 'path', dir: 'dir'])
        NotationDependency subResult = parser.parse([name: 'path/sub'])
        // then
        assert result.is(localDirectoryDependency)
        assert subResult.is(gitMercurialNotationDependency)
    }

    @Test
    void 'unrecognized notation with vendorPath should be delegated to VendorMapNotationParser'() {
        // when
        when(vendorNotationDependency.getName()).thenReturn('unrecognized')
        NotationDependency result = parser.parse([name: 'unrecognized', vendorPath: 'vendor/unrecognized'])
        NotationDependency subResult = parser.parse([name: 'unrecognized/sub'])
        // then
        assert result.is(vendorNotationDependency)
        assert subResult.is(vendorNotationDependency)
    }

    @Test
    void 'normal notation with vendorPath should be parsed successfully'() {
        // when
        NotationDependency result = parser.parse([name: 'path', vendorPath: 'vendor/path'])
        NotationDependency subResult = parser.parse([name: 'path/sub'])
        // then
        assert result.is(vendorNotationDependency)
        assert subResult.is(gitMercurialNotationDependency)
    }

    @Test
    void 'notation should be delegated to vcs parser'() {
        // given
        Map notation = [name: 'path', vcs: 'git']
        MockUtils.mockVcsService(MapNotationParser, Git, vcsMapNotationParser)

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse(captor.capture())
        VcsGolangPackage pkg = VcsGolangPackage.builder()
                .withVcsType(VcsType.GIT)
                .withPath('path')
                .withRootPath('path')
                .withUrl('url')
                .build()
        assertCaptorValue(captor.value, [name: 'path', 'package': pkg, vcs: 'git'])
    }

    @Test
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

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse([name: 'path', 'package': vcsPackage, vcs: ''])
        verify(gitMercurialNotationDependency).setUrls(['newUrl1', 'newUrl2'])
    }

    @Test
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

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse([name: 'root', 'package': vcsPackage])
    }

    @Test
    void 'unrecognized with url should be parsed successfully'() {
        // given
        Map notation = [name: 'unrecognized', url: 'url']
        // when
        parser.parse(notation)
        // then
        VcsGolangPackage pkg = VcsGolangPackage.builder()
                .withPath('unrecognized')
                .withRootPath('unrecognized')
                .withUrl('url')
                .withVcsType(VcsType.GIT)
                .withTemp(true)
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
        assert actualPkg.temp == expectedPkg.temp
    }

    @Test
    void 'unrecognized package with url and vcs should be parsed successfully'() {
        // given
        Map notation = [name: 'unrecognized', url: 'url', vcs: 'hg']
        MockUtils.mockVcsService(MapNotationParser, Mercurial, vcsMapNotationParser)
        // when
        parser.parse(notation)
        // then
        VcsGolangPackage pkg = VcsGolangPackage.builder()
                .withPath('unrecognized')
                .withRootPath('unrecognized')
                .withUrl('url')
                .withVcsType(VcsType.MERCURIAL)
                .withTemp(true)
                .build()

        verify(vcsMapNotationParser).parse(captor.capture())
        assertCaptorValue(captor.value, [name: 'unrecognized', url: 'url', vcs: 'hg', 'package': pkg])
    }

    @Test
    void 'unrecognized package with url substitution should be parsed successfully'() {
        // given
        Map notation = [name: 'unrecognized']
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
                .withTemp(true)
                .build()

        verify(vcsMapNotationParser).parse(captor.capture())
        assertCaptorValue(captor.value, [name: 'unrecognized', vcs: 'git', 'package': pkg])
    }

    @Test
    void 'url in unrecognized package will not be substituted'() {
        // given
        Map notation = [name: 'unrecognized', url: 'url1']
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
                .withTemp(true)
                .build()

        verify(vcsMapNotationParser).parse(captor.capture())
        assertCaptorValue(captor.value, [name: 'unrecognized', url: 'url1', vcs: 'git', 'package': pkg])
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
