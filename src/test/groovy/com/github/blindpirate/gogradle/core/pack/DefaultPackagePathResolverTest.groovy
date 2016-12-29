package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
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

    PackageInfo packageInfo = PackageInfo.builder().withPath('root/package')
            .withRootPath('root')
            .withVcsType(VcsType.Git)
            .withUrls(['url'])
            .build()

    DefaultPackagePathResolver resolver

    String packagePath = 'packagePath'

    @Before
    void setUp() {
        resolver = new DefaultPackagePathResolver([resolver1, resolver2])
        when(resolver1.produce(packagePath)).thenReturn(empty())
        when(resolver2.produce(packagePath)).thenReturn(of(packageInfo))
    }


    @Test
    void 'resolving a package should success'() {
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
    void 'package and its root package should be put into cache after successful resolution'() {
        // given
        PackageInfo info = PackageInfo.builder()
                .withPath('github.com/a/b/c')
                .withRootPath('github.com/a/b')
                .withVcsType(VcsType.Git)
                .withUrls([])
                .build()
        when(resolver1.produce('github.com/a/b/c')).thenReturn(of(info))
        // when
        resolver.produce('github.com/a/b/c')
        // then
        assert ReflectionUtils.getField(resolver, 'cache').size() == 2
    }

    @Test
    void 'root of an incomplete package should not be put into cache after resolution'() {
        // given
        when(resolver1.produce('gihub.com/a')).thenReturn(of(PackageInfo.INCOMPLETE))
        // when
        resolver.produce('gihub.com/a')
        // then
        assert ReflectionUtils.getField(resolver, 'cache').size() == 1
    }

    @Test(expected = PackageResolutionException)
    void 'exception should be thrown if unable to resolve'() {
        // given
        when(resolver2.produce(packagePath)).thenReturn(empty())

        // then
        resolver.produce(packagePath)
    }

    @Test
    void 'root package result should be leveraged when resolving children package'() {
        // given
        PackageInfo rootInfo = PackageInfo.builder()
                .withPath('github.com/a/b')
                .withRootPath('github.com/a/b')
                .withVcsType(VcsType.Git)
                .withUrls([])
                .build()
        getField(resolver, 'cache').put('github.com', PackageInfo.INCOMPLETE)
        getField(resolver, 'cache').put('github.com/a', PackageInfo.INCOMPLETE)
        getField(resolver, 'cache').put('github.com/a/b', rootInfo)

        // when
        PackageInfo result1 = resolver.produce('github.com/a/b').get()
        PackageInfo result2 = resolver.produce('github.com/a/b/c').get()
        PackageInfo result3 = resolver.produce('github.com/a/b/c/d').get()

        // then
        verify(resolver1, times(0)).produce(anyString())
        assert result1 == rootInfo
        assert result2.path == 'github.com/a/b/c'
        assert result3.path == 'github.com/a/b/c/d'
        assert allFieldsEquals(result2, rootInfo, ['vcsType', 'urls', 'rootPath'])
        assert allFieldsEquals(result3, rootInfo, ['vcsType', 'urls', 'rootPath'])
    }

}
