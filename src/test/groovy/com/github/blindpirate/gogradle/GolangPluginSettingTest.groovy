package com.github.blindpirate.gogradle

import org.junit.Before
import org.junit.Test

class GolangPluginSettingTest {
    GolangPluginSetting setting = new GolangPluginSetting()

    @Before
    void setUp() {
        setting.packageName = 'github.com/a/b'
    }

    @Test(expected = IllegalStateException)
    void 'verification should fail if package name not set'() {
        setting.packageName = ''
        setting.verify()
    }

//    @Test(expected = IllegalStateException)
//    void 'verification should fail if no go executable found'() {
//        setting.verify()
//    }

    private Process runGopathTestWithEnvs(String[] envs) {
        String currentClasspath = System.getProperty("java.class.path");
        String[] cmds = ['java', '-cp', currentClasspath, GopathTest.name] as String[]
        return Runtime.runtime.exec(cmds, envs)
    }

    @Test
    void 'verification should fail if useGlobalGopath==true but no GOPATH found'() {
        // when
        Process process = runGopathTestWithEnvs([] as String[])
        // then
        assert process.waitFor() == GopathTest.FAILURE_CODE
    }

    @Test
    void 'verification should success if useGlobalGopath==true and GOPATH found'() {
        // when
        Process process = runGopathTestWithEnvs(['GOPATH=/xxx'] as String[])
        // then
        assert process.waitFor() == GopathTest.SUCCESS_CODE
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
            ret.setPackageName("name");
            ret.setUseGlobalGopath(true);
            return ret;
        }
    }
}
