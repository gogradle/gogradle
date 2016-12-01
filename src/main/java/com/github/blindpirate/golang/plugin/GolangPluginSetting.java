package com.github.blindpirate.golang.plugin;

import com.github.blindpirate.golang.plugin.core.build.BuildMode;

public class GolangPluginSetting {
    private BuildMode buildMode;
    private String rootPackage;
    // should we use global GOPATH or isolate this build?
    // if true, files in global GOPATH will be used and modified just as go binary do
    private boolean globalGopath;

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
