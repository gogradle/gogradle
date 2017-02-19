package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.Key
import org.gradle.api.Project
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

import static org.mockito.Mockito.*

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

    @Test
    void 'offline field should be injected if it is null'() {
        // given
        Project deepMock = mock(Project, Mockito.RETURNS_DEEP_STUBS)
        when(GogradleGlobal.INSTANCE.getInstance(Project)).thenReturn(deepMock)
        when(deepMock.getGradle().getStartParameter().isOffline()).thenReturn(true)
        Boolean oldValue = ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'offline')
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', null)
        // then
        assert GogradleGlobal.isOffline()
        ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', oldValue)
    }
}
