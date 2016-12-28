package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.Arch;
import com.github.blindpirate.gogradle.core.Os;
import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.util.Assert;

import javax.inject.Singleton;

import java.util.List;

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
    private List<String> buildTags;
    private Arch hostArch;
    private Os hostOs;
    private Arch targetArch;
    private Os targetOs;
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

    public String getGlobalGopath() {
        if (globalGopath == null) {
            this.globalGopath = trimToNull(System.getenv("GOPATH"));
        }
        return globalGopath;
    }

    public List<String> getBuildTags() {
        return buildTags;
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
