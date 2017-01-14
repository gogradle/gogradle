package com.github.blindpirate.gogradle

import org.junit.Before
import org.junit.Test

import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult
import static com.github.blindpirate.gogradle.util.ProcessUtils.runProcessWithCurrentClasspath

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
    void 'setting build tags should succeed'() {
        setting.buildTags = ['a', 'b']
        assert setting.buildTags == ['a', 'b']
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
}
