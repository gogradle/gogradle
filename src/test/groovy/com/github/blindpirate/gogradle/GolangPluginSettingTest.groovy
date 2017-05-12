package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.mode.BuildMode
import org.junit.Before
import org.junit.Test

class GolangPluginSettingTest {
    GolangPluginSetting setting = new GolangPluginSetting()

    @Before
    void setUp() {
        setting.packagePath = 'github.com/a/b'
    }

    @Test(expected = IllegalStateException)
    void 'verification should fail if package name not set'() {
        setting.packagePath = ''
        setting.verify()
    }

    @Test
    void 'verification should succeed if package name is set'() {
        setting.verify()
    }

    @Test
    void 'setting build mode should succeed'() {
        setting.buildMode = 'DEVELOP'
        assert setting.buildMode == BuildMode.DEVELOP

        setting.buildMode = BuildMode.REPRODUCIBLE
        assert setting.buildMode == BuildMode.REPRODUCIBLE
    }

    @Test
    void 'setting build mode via System.property should succeed'() {
        setting.buildMode = 'DEVELOP'
        assert setting.buildMode == BuildMode.DEVELOP

        System.setProperty('gogradle.mode', 'REPRODUCIBLE')
        assert setting.buildMode == BuildMode.REPRODUCIBLE

        System.setProperty('gogradle.mode', '')
    }

    @Test
    void 'setting go executable should succeed'() {
        assert setting.goExecutable == 'go'

        setting.goExecutable = '/path/to/go'
        assert setting.goExecutable == '/path/to/go'
    }

    @Test
    void 'setting build tags should succeed'() {
        setting.buildTags = ['a', 'b']
        assert setting.buildTags == ['a', 'b']
    }

    @Test(expected = IllegalStateException)
    void 'setting build tags should fail when it contains single quote'() {
        setting.buildTags = ["'"]
    }

    @Test(expected = IllegalStateException)
    void 'setting build tags should fail when it contains double quote'() {
        setting.buildTags = ['"']
    }

    @Test
    void 'setting packagePath should succeed'() {
        assert setting.packagePath == 'github.com/a/b'
    }

    @Test
    void 'setting go version should succeed'() {
        setting.goVersion = '1.7.4'
        assert setting.goVersion == '1.7.4'
    }

    @Test
    void 'setting goExecutable should succeed'() {
        setting.goExecutable = '/bin/go'
        assert setting.goExecutable == '/bin/go'
    }

    @Test
    void 'setting fuckGfw should succeed'() {
        setting.fuckGfw = true
        assert setting.fuckGfw
    }

    @Test
    void 'setting goroot should succeed'() {
        setting.goRoot = 'goroot'
        assert setting.goRoot == 'goroot'
    }

    @Test
    void 'setting global cache time should succeed'() {
        assertCacheTimeEquals(1, 'second', 1)
        assertCacheTimeEquals(1, 'second', 1)
        assertCacheTimeEquals(1, 'minute', 60)
        assertCacheTimeEquals(1, 'minutes', 60)
        assertCacheTimeEquals(2, 'hour', 3600 * 2)
        assertCacheTimeEquals(2, 'hours', 3600 * 2)
        assertCacheTimeEquals(3, 'day', 3600 * 24 * 3)
        assertCacheTimeEquals(3, 'days', 3600 * 24 * 3)
    }

    @Test(expected = IllegalStateException)
    void 'setting an unsupported time unit should fail'() {
        setting.globalCacheFor(1, 'year')
    }

    void assertCacheTimeEquals(int count, String unit, long expectedResult) {
        setting.globalCacheFor(count, unit)
        assert setting.getGlobalCacheSecond() == expectedResult
    }


}
