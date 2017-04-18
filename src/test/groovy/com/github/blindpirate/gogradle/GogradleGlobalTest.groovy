package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.google.inject.Key
import org.junit.Test
import org.junit.runner.RunWith

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
@WithMockInjector
class GogradleGlobalTest {

    @Test
    void 'global injection should succeed'() {
        // given
        Key key = mock(Key)
        // when
        GogradleGlobal.getInstance(PackagePathResolver)
        GogradleGlobal.getInstance(key)
        // then
        verify(GogradleGlobal.INSTANCE.injector).getInstance(PackagePathResolver)
        verify(GogradleGlobal.INSTANCE.injector).getInstance(key)
    }
}
