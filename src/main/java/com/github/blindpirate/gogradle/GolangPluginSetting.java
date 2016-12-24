package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.util.Assert;

import javax.inject.Singleton;

import java.nio.charset.Charset;

import static com.github.blindpirate.gogradle.core.mode.BuildMode.Reproducible;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.valueOf;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;
import static com.github.blindpirate.gogradle.util.StringUtils.trimToNull;

@Singleton
public class GolangPluginSetting {
    // indicate the global GOPATH (in system environment variables)
    private boolean useGlobalGopath = false;
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int MAX_DIRECTORY_WALK_DEPTH = 100;
    private String globalGopath;
    private BuildMode buildMode = Reproducible;
    private String packageName;
    // should we use global GOPATH or isolate this build?
    // if true, files in global GOPATH will be used and modified just as go binary do
    // private boolean globalGopath;

    // e.g 1.1/1.7/1.7.3/1.8beta1
    private String goVersion;
    private String goExecutable;

    // if true, the plugin will make its best effort to bypass the GFW
    // designed for Chinese developer
    private boolean fuckGfw;

    public boolean isUseGlobalGopath() {
        return useGlobalGopath;
    }

    public void setUseGlobalGopath(boolean useGlobalGopath) {
        this.useGlobalGopath = useGlobalGopath;
    }

    public String getGlobalGopath() {
        if (globalGopath == null) {
            setGlobalGopath(System.getenv("GOPATH"));
        }
        return globalGopath;
    }

    public void setGlobalGopath(String globalGopath) {
        this.globalGopath = trimToNull(globalGopath);
    }

    public BuildMode getBuildMode() {
        return buildMode;
    }

    public void setBuildMode(String buildMode) {
        this.buildMode = valueOf(buildMode);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void verify() {
        verifyGlobalGopath();
//        verifyGoExecutable();
        verifyPackageName();
    }

    private void verifyPackageName() {
        Assert.isTrue(isNotBlank(packageName), "Package name must be specified!");
    }

//    private void verifyGoExecutable() {
//        try {
//            Process process = Runtime.getRuntime().exec("go version");
//            String consoleOutput = ProcessUtils.getOutput(process);
//            Assert.isTrue(consoleOutput.contains("version"), "Error in go version, output is:" + consoleOutput);
//        } catch (IOException | InterruptedException e) {
//            throw new IllegalStateException("Cannot find appropriate go executables, the cause is:" +
//                    e.getMessage());
//        }
//    }

    public void verifyGlobalGopath() {
        if (useGlobalGopath && getGlobalGopath() == null) {
            throw new IllegalStateException("useGlobalGopath is set but no $GOPATH is found!");
        }
    }
}
