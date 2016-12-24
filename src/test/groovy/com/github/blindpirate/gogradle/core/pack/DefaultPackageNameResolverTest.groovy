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
class DefaultPackageNameResolverTest {
    @Mock
    PackageNameResolver resolver1
    @Mock
    PackageNameResolver resolver2

    @Mock
    PackageInfo packageInfo

    DefaultPackageNameResolver resolver

    String packageName = 'packageName'

    @Before
    void setUp() {
        resolver = new DefaultPackageNameResolver([resolver1, resolver2])
        when(resolver1.produce(packageName)).thenReturn(empty())
        when(resolver2.produce(packageName)).thenReturn(of(packageInfo))
        when(packageInfo.getName()).thenReturn('package')
        when(packageInfo.getRootName()).thenReturn('root')
    }


    @Test
    void 'resolving a package should success'() {
        assert resolver.produce(packageName).get() == packageInfo
    }

    @Test
    void 'resolution result should be cached'() {
        // when
        resolver.produce(packageName)
        resolver.produce(packageName)
        // then
        verify(resolver2, times(1)).produce(packageName)
    }

    @Test
    void 'root package should be put into cache after successful resolution'() {
        // given
        PackageInfo info = PackageInfo.builder()
                .withName('github.com/a/b/c')
                .withRootName('github.com/a/b')
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
    void 'root package should not be put into cache after resolution to incoplete package'() {
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
        when(resolver2.produce(packageName)).thenReturn(empty())

        // then
        resolver.produce(packageName)
    }

    @Test
    void 'root package result should be leveraged when resolving children package'() {
        // given
        PackageInfo rootInfo = PackageInfo.builder()
                .withName('github.com/a/b')
                .withRootName('github.com/a/b')
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
        assert result2.name == 'github.com/a/b/c'
        assert result3.name == 'github.com/a/b/c/d'
        assert allFieldsEquals(result2, rootInfo, ['vcsType', 'urls', 'rootName'])
        assert allFieldsEquals(result3, rootInfo, ['vcsType', 'urls', 'rootName'])

    }

}
