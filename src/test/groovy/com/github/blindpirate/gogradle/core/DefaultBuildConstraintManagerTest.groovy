package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.crossplatform.Os
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultBuildConstraintManagerTest {
    DefaultBuildConstraintManager manager

    @Mock
    GolangPluginSetting setting
    @Mock
    GoBinaryManager goBinaryManager

    @Before
    void setUp() {
        manager = new DefaultBuildConstraintManager(goBinaryManager, setting)
    }

    @Test
    void 'predefined constraints should be added'() {
        // given
        when(goBinaryManager.getGoVersion()).thenReturn("1.1")
        // when
        manager.prepareConstraints()
        // then
        assert manager.getAllConstraints().contains(Os.getHostOs().toString())
        assert manager.getAllConstraints().contains(Arch.getHostArch().toString())
        assert manager.getAllConstraints().contains("go1.1")
        assert !manager.getAllConstraints().contains("go1.2")
        assert !manager.getAllConstraints().contains("go1.0")
    }

    @Test
    void 'all old versions should be added'() {
        // given
        when(goBinaryManager.getGoVersion()).thenReturn("1.100")
        // when
        manager.prepareConstraints()
        // then
        (1..100).each { assert manager.getAllConstraints().contains("go1." + it) }
    }

    @Test(expected = IllegalStateException)
    void 'only go1 is supported'() {
        when(goBinaryManager.getGoVersion()).thenReturn('2.0.0')
        manager.prepareConstraints()
    }
}
