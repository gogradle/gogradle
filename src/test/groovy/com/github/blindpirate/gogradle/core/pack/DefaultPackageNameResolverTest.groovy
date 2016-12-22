package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.google.common.base.Optional.absent
import static com.google.common.base.Optional.of
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
        when(resolver1.produce(packageName)).thenReturn(absent())
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

    @Test
    void 'absent result should be returned if unable to resolve'() {
        // given
        when(resolver2.produce(packageName)).thenReturn(absent())

        // then
        assert !resolver.produce(packageName).isPresent()
    }

}
