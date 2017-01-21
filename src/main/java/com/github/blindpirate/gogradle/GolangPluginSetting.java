package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.blindpirate.gogradle.core.mode.BuildMode.REPRODUCIBLE;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.valueOf;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;

@Singleton
public class GolangPluginSetting {
    static final Pattern TARGET_PLATFORM_PATTERN
            = Pattern.compile("(\\s*\\w+\\-\\w+\\s*)(,\\s*\\w+\\-\\w+\\s*)*");

    private BuildMode buildMode = REPRODUCIBLE;
    private String packagePath;
    private List<String> buildTags = new ArrayList<>();
    private List<String> extraBuildArgs = new ArrayList<>();
    private List<String> extraTestArgs = new ArrayList<>();
    private String outputLocation;
    private String outputPattern = "${os}_${arch}_${projectName}${extension}";
    private String targetPlatform;

    // e.g 1.1/1.7/1.7.3/1.8beta1
    private String goVersion;
    private String goExecutable;

    // if true, the plugin will make its best effort to bypass the GFW
    // designed for Chinese developer
    private boolean fuckGfw;

    public String getGoExecutable() {
        return goExecutable == null ? "go" : goExecutable;
    }

    public void setBuildMode(String buildMode) {
        this.buildMode = valueOf(buildMode);
    }

    public BuildMode getBuildMode() {
        return buildMode;
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

    public List<String> getExtraBuildArgs() {
        return extraBuildArgs;
    }

    public void setExtraBuildArgs(List<String> extraBuildArgs) {
        this.extraBuildArgs = extraBuildArgs;
    }

    public List<String> getExtraTestArgs() {
        return extraTestArgs;
    }

    public void setExtraTestArgs(List<String> extraTestArgs) {
        this.extraTestArgs = extraTestArgs;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    public String getOutputPattern() {
        return outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
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

    public void verify() {
        verifyPackagePath();
        verifyTargetPlatform();
    }

    private void verifyTargetPlatform() {
        if (StringUtils.isNotBlank(targetPlatform)) {
            Assert.isTrue(TARGET_PLATFORM_PATTERN.matcher(targetPlatform).matches());
        }
    }

    private void verifyPackagePath() {
        Assert.isTrue(isNotBlank(packagePath), "Package name must be specified!");
    }
}
