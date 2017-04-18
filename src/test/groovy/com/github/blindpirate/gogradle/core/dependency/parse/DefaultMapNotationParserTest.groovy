package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage
import com.github.blindpirate.gogradle.core.StandardGolangPackage
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedPackageNotationDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.Mercurial
import com.github.blindpirate.gogradle.vcs.VcsType
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

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
    MapNotationParser gitMapNotationParser
    @Mock
    MapNotationParser mercurialMapNotationParser
    @Mock
    GogradleRootProject gogradleRootProject
    @Captor
    ArgumentCaptor captor

    @Before
    void setUp() {
        parser = new DefaultMapNotationParser(dirMapNotationParser, vendorMapNotationParser, packagePathResolver, gogradleRootProject)
        MockUtils.mockVcsService(MapNotationParser, Git, gitMapNotationParser)
        MockUtils.mockVcsService(MapNotationParser, Mercurial, mercurialMapNotationParser)
        when(packagePathResolver.produce(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                String path = invocation.getArgument(0)
                if (path.startsWith('unrecognized')) {
                    return of(UnrecognizedGolangPackage.of(path))
                } else if (path.startsWith("original")) {
                    return of(VcsGolangPackage.builder()
                            .withPath(path)
                            .withRootPath('original')
                            .withOriginalVcsInfo(VcsType.GIT, ['originalUrl'])
                            .build())
                } else if (path.startsWith('substituted')) {
                    return of(VcsGolangPackage.builder()
                            .withPath(path)
                            .withRootPath('substituted')
                            .withSubstitutedVcsInfo(VcsType.GIT, ['substitutedUrl'])
                            .build())
                } else if (path.startsWith('local')) {
                    return of(LocalDirectoryGolangPackage.of('local', path, 'dir'))
                } else {
                    assert false
                }
            }
        })
    }

    @Test(expected = IllegalStateException)
    void 'notation without name should be rejected'() {
        parser.parse([:])
    }

    @Test
    void 'parsing root project should succeed'() {
        assert parser.parse([name: 'GOGRADLE_ROOT']).is(gogradleRootProject)
    }

    @Test
    void 'unrecognized package with dir should be delegated to DirMapNotationParser'() {
        // when
        parser.parse([name: 'unrecognized', dir: 'dir'])
        // then
        verify(dirMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'unrecognized'
        assert captor.value.dir == 'dir'
        assert captor.value.package instanceof UnrecognizedGolangPackage
    }

    @Test
    void 'unrecognized notation dependency should be generated if url not specified'() {
        assert parser.parse([name: 'unrecognized', useless: 'useless']) instanceof UnrecognizedPackageNotationDependency
    }

    @Test
    void 'local directory package should be delegated to DirMapNotationParser'() {
        // when
        parser.parse([name: 'local'])
        // then
        verify(dirMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'local'
        assert captor.value.package instanceof LocalDirectoryGolangPackage
        assert captor.value.package.rootPathString == 'local'
        assert captor.value.package.pathString == 'local'
        assert captor.value.package.dir == 'dir'
    }

    @Test
    void 'local/sub directory package should be delegated to DirMapNotationParser'() {
        // when
        parser.parse([name: 'local/sub'])
        // then
        verify(dirMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'local'
        assert captor.value.package instanceof LocalDirectoryGolangPackage
        assert captor.value.package.rootPathString == 'local'
        assert captor.value.package.pathString == 'local'
        assert captor.value.package.dir == 'dir'
    }

    @Test
    void 'unrecognized dependency should be parsed successfully if url and vcs is provided'() {
        // when
        parser.parse([name: 'unrecognized', url: 'url', vcs: 'hg'])
        // then
        verify(mercurialMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'unrecognized'
        assert captor.value.url == 'url'
        assert captor.value.package instanceof VcsGolangPackage
        assert captor.value.package.originalVcsInfo == null
        assert captor.value.package.pathString == 'unrecognized'
        assert captor.value.package.rootPathString == 'unrecognized'
        assert captor.value.package.substitutedVcsInfo.vcsType == VcsType.MERCURIAL
        assert captor.value.package.substitutedVcsInfo.urls == ['url']
    }

    @Test
    void 'unrecognized dependency should be parsed successfully if only url is provided'() {
        // when
        parser.parse([name: 'unrecognized', url: 'url'])
        // then
        verify(gitMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'unrecognized'
        assert captor.value.url == 'url'
        assert captor.value.package instanceof VcsGolangPackage
        assert captor.value.package.originalVcsInfo == null
        assert captor.value.package.pathString == 'unrecognized'
        assert captor.value.package.rootPathString == 'unrecognized'
        assert captor.value.package.substitutedVcsInfo.vcsType == VcsType.GIT
        assert captor.value.package.substitutedVcsInfo.urls == ['url']
    }

    @Test
    void 'vcs package should be parsed successfully'() {
        // when
        parser.parse([name: 'original'])
        // then
        verify(gitMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'original'
        assert captor.value.package instanceof VcsGolangPackage
        assert captor.value.package.pathString == 'original'
        assert captor.value.package.rootPathString == 'original'
        assert captor.value.package.originalVcsInfo.vcsType == VcsType.GIT
        assert captor.value.package.originalVcsInfo.urls == ['originalUrl']
        assert captor.value.package.substitutedVcsInfo == null
    }

    @Test
    void 'vcs sub package should be parsed successfully'() {
        // when
        parser.parse([name: 'original/sub'])
        // then
        verify(gitMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'original'
        assert captor.value.package instanceof VcsGolangPackage
        assert captor.value.package.pathString == 'original'
        assert captor.value.package.rootPathString == 'original'
        assert captor.value.package.originalVcsInfo.vcsType == VcsType.GIT
        assert captor.value.package.originalVcsInfo.urls == ['originalUrl']
        assert captor.value.package.substitutedVcsInfo == null
    }

    @Test
    void 'vcs package with url and vcs should be parsed successfully'() {
        // given
        reset(packagePathResolver)
        when(packagePathResolver.produce(anyString())).thenReturn(of(
                VcsGolangPackage.builder()
                        .withRootPath('substituted')
                        .withPath('substituted')
                        .withSubstitutedVcsInfo(VcsType.MERCURIAL, ['substitutedUrl'])
                        .build()
        ))
        // when
        parser.parse([name: 'substituted', vcs: 'hg', url: 'url'])
        // then
        verify(mercurialMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'substituted'
        assert captor.value.package instanceof VcsGolangPackage
        assert captor.value.package.pathString == 'substituted'
        assert captor.value.package.rootPathString == 'substituted'
        assert captor.value.package.originalVcsInfo == null
        assert captor.value.package.substitutedVcsInfo.vcsType == VcsType.MERCURIAL
        assert captor.value.package.substitutedVcsInfo.urls == ['substitutedUrl']
    }

    @Test
    void 'unrecognized notation with vendorPath should be delegated to VendorMapNotationParser'() {
        // when
        parser.parse([name: 'unrecognized', vendorPath: 'vendor/unrecognized'])
        // then
        verify(vendorMapNotationParser).parse(captor.capture())
        assert captor.value.name == 'unrecognized'
        assert captor.value.vendorPath == 'vendor/unrecognized'
        assert captor.value.package instanceof UnrecognizedGolangPackage
    }

    @Test(expected = IllegalStateException)
    void 'notation with mismatched vcs should result in an exception'() {
        parser.parse([name: 'original', vcs: 'svn'])
    }

    @Test(expected = DependencyResolutionException)
    void 'only VcsGolangPackage and UnrecognizedGolangPackage can be parsed'() {
        // given
        reset(packagePathResolver)
        when(packagePathResolver.produce('fmt')).thenReturn(of(StandardGolangPackage.of('fmt')))
        parser.parse([name: 'fmt'])
    }
}
