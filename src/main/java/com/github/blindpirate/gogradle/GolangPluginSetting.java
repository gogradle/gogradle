/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.ImmutableMap;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.mode.BuildMode.DEVELOP;
import static com.github.blindpirate.gogradle.core.mode.BuildMode.REPRODUCIBLE;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;

/**
 * Stores global configurations for Gogradle.
 * A typical usage is in build.gradle:
 * <pre>
 *     {@code
 *          golang {
 *              packagePath = 'github.com/my/package'
 *              goVersion = '1.8.3'
 *              ...
 *          }
 *     }
 * </pre>
 */
@Singleton
public class GolangPluginSetting {
    private static final Map<String, BuildMode> BUILD_MODE_MAP = ImmutableMap.<String, BuildMode>builder()
            .put(DEVELOP.getAbbr(), DEVELOP)
            .put(DEVELOP.toString(), DEVELOP)
            .put(REPRODUCIBLE.getAbbr(), REPRODUCIBLE)
            .put(REPRODUCIBLE.toString(), REPRODUCIBLE)
            .build();
    private BuildMode buildMode = REPRODUCIBLE;

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

    // A user defined source for go binaries
    private URI goBinaryDownloadRootUri;

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
        String mode = GogradleGlobal.getMode();
        if (StringUtils.isNotEmpty(mode)) {
            return Assert.isNotNull(BUILD_MODE_MAP.get(mode));
        } else {
            return buildMode;
        }
    }

    public void setBuildMode(String buildMode) {
        this.buildMode = Assert.isNotNull(BUILD_MODE_MAP.get(buildMode));
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
        buildTags.forEach(t -> Assert.isTrue(!t.contains("\"") && !t.contains("'")));
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

    public URI getGoBinaryDownloadBaseUri() {
        return goBinaryDownloadRootUri;
    }

    public void setGoBinaryDownloadBaseUri(String goBinaryDownloadBaseUri) {
        setGoBinaryDownloadBaseUri(goBinaryDownloadBaseUri == null ? null : URI.create(goBinaryDownloadBaseUri));
    }

    public void setGoBinaryDownloadBaseUri(URI goBinaryDownloadBaseUrl) {
        this.goBinaryDownloadRootUri = goBinaryDownloadBaseUrl;
    }

    public void globalCacheFor(int count, TimeUnit timeUnit) {
        globalCacheSecond = timeUnit.toSeconds(count);
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
