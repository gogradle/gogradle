package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.google.common.base.Optional
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

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
        when(resolver1.accept(packageName)).thenReturn(false)
        when(resolver2.accept(packageName)).thenReturn(true)
        when(resolver2.produce(packageName)).thenReturn(packageInfo)
    }

    @Test
    void 'any package name should be accepted'() {
        assert resolver.accept('')
    }

    @Test
    void 'resolving a package should success'() {
        assert resolver.produce(packageName) == packageInfo
    }

    @Test
    void 'resolution result should be cached'() {
        // when
        resolver.produce(packageName)
        resolver.produce(packageName)
        // then
        verify(resolver2, times(1)).produce(packageName)
    }

    @Test(expected = DependencyResolutionException)
    void 'exception should be throwed if unable to resolve'() {
        // given
        when(resolver2.accept(packageName)).thenReturn(false)

        // then
        resolver.produce(packageName)
    }

}
