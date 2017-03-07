package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.core.mode.BuildMode.REPRODUCIBLE;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.valueOf;
import static com.github.blindpirate.gogradle.crossplatform.Arch.getHostArch;
import static com.github.blindpirate.gogradle.crossplatform.Os.getHostOs;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Singleton
public class GolangPluginSetting {
    private static final Pattern TARGET_PLATFORM_PATTERN
            = Pattern.compile("(\\s*\\w+\\-\\w+\\s*)(,\\s*\\w+\\-\\w+\\s*)*");

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
    private String packagePath;
    private List<String> buildTags = new ArrayList<>();
    private List<String> extraBuildArgs = new ArrayList<>();
    private List<String> extraTestArgs = new ArrayList<>();
    private String outputLocation = GogradleGlobal.GOGRADLE_BUILD_DIR_NAME;
    private String outputPattern = "${os}_${arch}_${packageName}";
    private List<Pair<Os, Arch>> targetPlatforms = asList(Pair.of(getHostOs(), getHostArch()));
    private long globalCacheSecond = 24 * 3600;

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
        Assert.isNotBlank(outputLocation, "outputLocation cannot be blank!");
        this.outputLocation = outputLocation;
    }

    public String getOutputPattern() {
        return outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        Assert.isNotBlank(outputPattern, "outputPattern cannot be blank!");
        this.outputPattern = outputPattern;
    }

    public List<Pair<Os, Arch>> getTargetPlatforms() {
        return targetPlatforms;
    }

    public void setTargetPlatform(String targetPlatform) {
        Matcher matcher = TARGET_PLATFORM_PATTERN.matcher(targetPlatform);
        Assert.isTrue(matcher.matches(),
                "Illegal target platform:" + targetPlatform);
        this.targetPlatforms = extractPlatforms(targetPlatform);
    }

    private List<Pair<Os, Arch>> extractPlatforms(String targetPlatform) {
        String[] platforms = StringUtils.splitAndTrim(targetPlatform, ",");
        return Stream.of(platforms).map(this::extractOne).collect(toList());
    }

    private Pair<Os, Arch> extractOne(String osAndArch) {
        String[] osArch = StringUtils.splitAndTrim(osAndArch, "\\-");
        Os os = Os.of(osArch[0]);
        Arch arch = Arch.of(osArch[1]);
        return Pair.of(os, arch);
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
        Assert.isTrue(isNotBlank(packagePath), "Package name must be specified!");
    }
}
