package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.util.Assert;

import javax.inject.Singleton;
import java.util.List;

import static com.github.blindpirate.gogradle.core.mode.BuildMode.REPRODUCIBLE;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.valueOf;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;

@Singleton
public class GolangPluginSetting {
    public static final String GOGRADLE_VERSION = "0.1.0";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int MAX_DFS_DEPTH = 100;

    private BuildMode buildMode = REPRODUCIBLE;
    private String packagePath;
    private List<String> buildTags;

    // e.g 1.1/1.7/1.7.3/1.8beta1
    private String goVersion;
    private String goExecutable;

    // if true, the plugin will make its best effort to bypass the GFW
    // designed for Chinese developer
    private boolean fuckGfw;

    public String getGoExecutable() {
        return goExecutable;
    }

    public boolean isFuckGfw() {
        return fuckGfw;
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

    public String getPackagePath() {
        return packagePath;
    }

    public void verify() {
        verifyPackagePath();
    }

    public String getGoVersion() {
        return goVersion;
    }

    private void verifyPackagePath() {
        Assert.isTrue(isNotBlank(packagePath), "Package name must be specified!");
    }
}
