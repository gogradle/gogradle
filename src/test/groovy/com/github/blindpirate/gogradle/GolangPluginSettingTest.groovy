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
    void 'verification should fail if useGlobalGopath==true but no GOPATH found'() {
        // when
        ProcessResult result = runProcessWithCurrentClasspath(GopathTest, [], [:])
        // then
        assert result.code == GopathTest.FAILURE_CODE
    }

    @Test
    void 'verification should success if useGlobalGopath==true and GOPATH found'() {
        // when
        ProcessResult result = runProcessWithCurrentClasspath(GopathTest, [], [GOPATH: 'xxx'])
        // then
        assert result.code == GopathTest.SUCCESS_CODE
    }

    @Test
    void 'setting build tags should success'() {
        // when
        setting.buildTags = ['a', 'b']
        assert setting.buildTags == ['a', 'b']
    }


    public static class GopathTest {
        public static final int SUCCESS_CODE = 0;
        public static final int FAILURE_CODE = 1;

        public static void main(String[] args) {
            try {
                GolangPluginSetting setting = newInstance();
                setting.verify();
                System.exit(SUCCESS_CODE);
            } catch (IllegalStateException e) {
                System.exit(FAILURE_CODE);
            }
        }

        private static GolangPluginSetting newInstance() {
            GolangPluginSetting ret = new GolangPluginSetting();
            ret.packagePath = "path";
            ret.useGlobalGopath = true;
            return ret;
        }
    }
}
