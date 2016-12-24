package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static java.util.Optional.empty
import static java.util.Optional.of
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

    @Test(expected = PackageResolutionException)
    void 'exception should be thrown if unable to resolve'() {
        // given
        when(resolver2.produce(packageName)).thenReturn(empty())

        // then
        resolver.produce(packageName)
    }

}
