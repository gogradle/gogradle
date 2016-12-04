package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.mode.BuildMode;

import javax.inject.Inject;

public class GolangPluginSetting {
    // indicate the global GOPATH (in system environment variables)
    private String globalGopath;
    private BuildMode buildMode;
    private String rootPackage;
    // should we use global GOPATH or isolate this build?
    // if true, files in global GOPATH will be used and modified just as go binary do
    // private boolean globalGopath;

    // e.g 1.1/1.7/1.7.3/1.8beta1
    private String goVersion;

    // if true, the plugin will make its best effort to bypass the GFW
    // designed for Chinese developer
    private boolean fuckGfw;

    @Inject
    public GolangPluginSetting() {

    }

    public String getGlobalGopath() {
        if (globalGopath == null) {
            globalGopath = System.getenv("GOPATH");
        }
        return globalGopath;
    }

    public void setGlobalGopath(String globalGopath) {
        this.globalGopath = globalGopath;
    }

    public BuildMode getBuildMode() {
        return buildMode;
    }

    public void setBuildMode(String buildMode) {
        this.buildMode = BuildMode.valueOf(buildMode);
    }

    public String getRootPackage() {
        return rootPackage;
    }

    public void setRootPackage(String rootPackage) {
        this.rootPackage = rootPackage;
    }
}
