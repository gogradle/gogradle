package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.google.inject.Key
import org.junit.Test
import org.junit.runner.RunWith

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class GogradleGlobalTest extends MockInjectorSupport {

    @Test
    void 'global injection should succeed'() {
        // given
        Key key = mock(Key)
        // when
        GogradleGlobal.getInstance(PackagePathResolver)
        GogradleGlobal.getInstance(key)
        // then
        verify(injector).getInstance(PackagePathResolver)
        verify(injector).getInstance(key)
    }
}
