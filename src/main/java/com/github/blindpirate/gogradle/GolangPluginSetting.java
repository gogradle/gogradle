package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.ImmutableMap;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.blindpirate.gogradle.core.mode.BuildMode.REPRODUCIBLE;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.valueOf;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;

@Singleton
public class GolangPluginSetting {
    private static final Map<String, TimeUnit> TIME_UNIT_MAP = ImmutableMap.<String, TimeUnit>builder()
            .put("second", TimeUnit.SECONDS)
            .put("seconds", TimeUnit.SECONDS)
            .put("minute", TimeUnit.MINUTES)
            .put("minutes", TimeUnit.MINUTES)
            .put("hour", TimeUnit.HOURS)
            .put("hours", TimeUnit.HOURS)
            .put("day", TimeUnit.DAYS)
            .put("days", TimeUnit.DAYS)
            .build();
    private BuildMode buildMode = REPRODUCIBLE;
    private static final String BUILD_MODE_KEY = "gogradle.mode";

    private String packagePath;
    private List<String> buildTags = new ArrayList<>();
    private long globalCacheSecond = 5 * 60;

    // e.g 1.1/1.7/1.7.3/1.8beta1
    private String goVersion;
    private String goExecutable;
    private String goRoot;

    // if true, the plugin will make its best effort to bypass the GFW
    // designed for Chinese developer
    private boolean fuckGfw;

    public String getGoRoot() {
        return goRoot;
    }

    public void setGoRoot(String goRoot) {
        this.goRoot = goRoot;
    }

    public String getGoExecutable() {
        return goExecutable == null ? "go" : goExecutable;
    }

    public BuildMode getBuildMode() {
        String mode = System.getProperty(BUILD_MODE_KEY);
        if (StringUtils.isNotEmpty(mode)) {
            return BuildMode.valueOf(mode);
        } else {
            return buildMode;
        }
    }

    public void setBuildMode(String buildMode) {
        this.buildMode = valueOf(buildMode);
    }

    public void setBuildMode(BuildMode buildMode) {
        this.buildMode = buildMode;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public List<String> getBuildTags() {
        return buildTags;
    }

    public void setBuildTags(List<String> buildTags) {
        this.buildTags = buildTags;
    }


    public String getGoVersion() {
        return goVersion;
    }

    public void setGoVersion(String goVersion) {
        this.goVersion = goVersion;
    }

    public void setGoExecutable(String goExecutable) {
        this.goExecutable = goExecutable;
    }

    public boolean isFuckGfw() {
        return fuckGfw;
    }

    public void setFuckGfw(boolean fuckGfw) {
        this.fuckGfw = fuckGfw;
    }

    public void globalCacheFor(int count, String timeUnit) {
        TimeUnit unit = TIME_UNIT_MAP.get(timeUnit);
        Assert.isTrue(unit != null, "Time unit " + timeUnit + " is not supported!");
        globalCacheSecond = unit.toSeconds(count);
    }

    public long getGlobalCacheSecond() {
        return globalCacheSecond;
    }

    public void verify() {
        verifyPackagePath();
    }

    private void verifyPackagePath() {
        Assert.isTrue(isNotBlank(packagePath), "Package's import path must be specified!");
    }
}
