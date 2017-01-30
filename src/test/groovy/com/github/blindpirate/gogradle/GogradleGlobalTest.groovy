package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.MockOffline
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.Key
import org.gradle.api.Project
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

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

    @Test
    @MockOffline
    void 'offline field should be injected if it is null'() {
        // given
        Project deepMock = mock(Project, Mockito.RETURNS_DEEP_STUBS)
        when(injector.getInstance(Project)).thenReturn(deepMock)
        when(deepMock.getGradle().getStartParameter().isOffline()).thenReturn(true)
        // then
        assert GogradleGlobal.isOffline()
    }
}
