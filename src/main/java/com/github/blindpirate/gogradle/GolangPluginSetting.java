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
import com.github.blindpirate.gogradle.crossplatform.DefaultGoBinaryManager;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    // https://storage.googleapis.com/golang/go1.7.4.windows-386.zip
    // https://storage.googleapis.com/golang/go1.7.4.linux-amd64.tar.gz
    private static final String BINARY_URL = "https://storage.googleapis.com/golang/" + DefaultGoBinaryManager.FILENAME;

    // http://golangtc.com/static/go/1.7.4/go1.7.4.windows-386.zip
    // http://golangtc.com/static/go/1.7.4/go1.7.4.linux-amd64.tar.gz
    private static final String BINARY_URL_GFW = "http://golangtc.com/static/go/${version}/"
            + DefaultGoBinaryManager.FILENAME;

    private BuildMode buildMode = REPRODUCIBLE;

    private String packagePath;
    private List<String> buildTags = new ArrayList<>();
    private long globalCacheSecond = 5 * 60;

    // e.g 1.1/1.7/1.7.3/1.8beta1
    private String goVersion;
    private String goExecutable;
    private String goRoot;

    private String goBinaryDownloadTemplate = BINARY_URL;

    public String getGoRoot() {
        return goRoot;
    }

    public void setGoRoot(String goRoot) {
        this.goRoot = goRoot;
    }

    public String getGoExecutable() {
        return goExecutable == null ? "go" : goExecutable;
    }

    @Nonnull
    public BuildMode getBuildMode() {
        String mode = GogradleGlobal.getMode();
        if (StringUtils.isNotEmpty(mode)) {
            return BuildMode.fromString(mode);
        } else {
            return buildMode;
        }
    }

    public void setBuildMode(@Nonnull String buildMode) {
        this.buildMode = BuildMode.fromString(buildMode);
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

    /** @deprecated {@link #getGoBinaryDownloadTemplate} */
    public boolean isFuckGfw() {
        return BINARY_URL_GFW.equals(goBinaryDownloadTemplate);
    }

    /** @deprecated {@link #setGoBinaryDownloadTemplate} */
    public void setFuckGfw(boolean fuckGfw) {
        if (fuckGfw) {
            fuckGfw();
        } else {
            setGoBinaryDownloadTemplate(BINARY_URL);
        }
    }

    public void fuckGfw() {
        goBinaryDownloadTemplate = BINARY_URL_GFW;
    }

    public String getGoBinaryDownloadTemplate() {
        return goBinaryDownloadTemplate;
    }

    /** @deprecated use {@link #setGoBinaryDownloadTemplate} */
    public void setGoBinaryDownloadBaseUri(String goBinaryDownloadBaseUri) {
        setGoBinaryDownloadBaseUri(goBinaryDownloadBaseUri == null ? null : URI.create(goBinaryDownloadBaseUri));
    }

    /** @deprecated use {@link #setGoBinaryDownloadTemplate} */
    public void setGoBinaryDownloadBaseUri(URI goBinaryDownloadBaseUri) {
        setGoBinaryDownloadTemplate(goBinaryDownloadBaseUri == null ? null
                : goBinaryDownloadBaseUri.resolve("/") + DefaultGoBinaryManager.FILENAME);
    }

    public void setGoBinaryDownloadTemplate(URI goBinaryDownloadTemplateUri) {
        setGoBinaryDownloadTemplate(goBinaryDownloadTemplateUri == null ? null
                : goBinaryDownloadTemplateUri.toASCIIString());
    }

    public void setGoBinaryDownloadTemplate(String goBinaryDownloadTemplate) {
        this.goBinaryDownloadTemplate = goBinaryDownloadTemplate;
    }

    public void globalCacheFor(int duration, @Nonnull String timeUnit) {
        if (!timeUnit.toUpperCase().endsWith("S")) {
            timeUnit += "S";
        }
        globalCacheFor(duration, TimeUnit.valueOf(timeUnit.toUpperCase()));
    }

    public void globalCacheFor(int duration, @Nonnull TimeUnit timeUnit) {
        globalCacheSecond = timeUnit.toSeconds(duration);
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
