package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.ReflectionUtils.allFieldsEquals
import static com.github.blindpirate.gogradle.util.ReflectionUtils.getField
import static java.util.Optional.empty
import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class DefaultPackagePathResolverTest {
    @Mock
    PackagePathResolver resolver1
    @Mock
    PackagePathResolver resolver2

    GolangPackage packageInfo = MockUtils.mockPackage()

    DefaultPackagePathResolver resolver

    String packagePath = 'packagePath'

    @Before
    void setUp() {
        resolver = new DefaultPackagePathResolver([resolver1, resolver2])
        when(resolver1.produce(packagePath)).thenReturn(empty())
        when(resolver2.produce(packagePath)).thenReturn(of(packageInfo))
    }


    @Test
    void 'resolving a package should succeed'() {
        assert resolver.produce(packagePath).get() == packageInfo
    }

    @Test
    void 'resolution result should be cached'() {
        // when
        resolver.produce(packagePath)
        resolver.produce(packagePath)
        // then
        verify(resolver2, times(1)).produce(packagePath)
    }

    @Test
    void 'package and its ancestor package should be put into cache after successful resolution'() {
        // given
        GolangPackage info = VcsGolangPackage.builder()
                .withPath('github.com/a/b/c')
                .withRootPath('github.com/a/b')
                .withVcsType(VcsType.GIT)
                .build()
        when(resolver1.produce('github.com/a/b/c')).thenReturn(of(info))
        // when
        resolver.produce('github.com/a/b/c')
        // then
        assert ReflectionUtils.getField(resolver, 'cache').size() == 4
    }

    @Test
    void 'root of an incomplete package should not be put into cache after resolution'() {
        // given
        when(resolver1.produce('github.com/a')).thenReturn(of(IncompleteGolangPackage.of('github.com/a')))
        // when
        resolver.produce('github.com/a')
        // then
        assert ReflectionUtils.getField(resolver, 'cache').size() == 2
    }


    @Test
    void 'root package result should be leveraged when resolving children package'() {
        // given
        GolangPackage rootInfo = VcsGolangPackage.builder()
                .withPath('github.com/a/b')
                .withRootPath('github.com/a/b')
                .withVcsType(VcsType.GIT)
                .build()
        getField(resolver, 'cache').put('github.com', IncompleteGolangPackage.of('github.com'))
        getField(resolver, 'cache').put('github.com/a', IncompleteGolangPackage.of('github.com/a'))
        getField(resolver, 'cache').put('github.com/a/b', rootInfo)

        // when
        GolangPackage result1 = resolver.produce('github.com/a/b').get()
        GolangPackage result2 = resolver.produce('github.com/a/b/c').get()
        GolangPackage result3 = resolver.produce('github.com/a/b/c/d').get()

        // then
        verify(resolver1, times(0)).produce(anyString())
        assert result1 == rootInfo
        assert result2.path == 'github.com/a/b/c'
        assert result3.path == 'github.com/a/b/c/d'
        assert allFieldsEquals(result2, rootInfo, ['vcsType', 'url', 'rootPath'])
        assert allFieldsEquals(result3, rootInfo, ['vcsType', 'url', 'rootPath'])
    }
}
