package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
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

    @Test
    void 'setting extraBuildArgs should succeed'() {
        setting.extraBuildArgs = ['']
        assert setting.extraBuildArgs == ['']
    }

    @Test
    void 'setting extraTestArgs should succeed'() {
        setting.extraTestArgs = ['1']
        assert setting.extraTestArgs == ['1']
    }

    @Test(expected = IllegalStateException)
    void 'setting blank outputLocation should result in an exception'() {
        setting.outputLocation = ' '
    }

    @Test(expected = IllegalStateException)
    void 'setting blank outputPattern should result in an exception'() {
        setting.outputPattern = ' '
    }

    @Test(expected = IllegalStateException)
    void 'setting illegal target platform should result in an exception'() {
        setting.targetPlatform = 'a-b,'
    }

    @Test(expected = IllegalArgumentException)
    void 'setting illegal os or arch should result in an exception'() {
        setting.targetPlatform = 'a-b'
    }

    @Test(expected = IllegalArgumentException)
    void 'os must located at left'() {
        setting.targetPlatform = 'amd64-linux'
    }

    @Test
    void 'setting target platform should succeed'() {
        setting.targetPlatform = 'windows-amd64, linux-amd64, linux-386'
        assert setting.targetPlatforms[0].left == Os.WINDOWS
        assert setting.targetPlatforms[0].right == Arch.AMD64
        assert setting.targetPlatforms[1].left == Os.LINUX
        assert setting.targetPlatforms[1].right == Arch.AMD64
        assert setting.targetPlatforms[2].left == Os.LINUX
        assert setting.targetPlatforms[2].right == Arch.I386
    }

    @Test
    void 'pattern matching targetPlatform should be correct'() {
        assert isValidTargetPlatform('a-b')
        assert isValidTargetPlatform('\t a-b \n')
        assert !isValidTargetPlatform(' a -b ')
        assert !isValidTargetPlatform('\ta-b,\n')
        assert !isValidTargetPlatform('a-b,')
        assert !isValidTargetPlatform(' a-b, ')
        assert !isValidTargetPlatform(',a-b,')
        assert isValidTargetPlatform('a-b,1-a,c-d')
        assert isValidTargetPlatform('\t\t\na-b\n ,\n 1-a\t\n , c-2d  ')
    }

    boolean isValidTargetPlatform(String value) {
        return GolangPluginSetting.TARGET_PLATFORM_PATTERN.matcher(value).matches()
    }
}
