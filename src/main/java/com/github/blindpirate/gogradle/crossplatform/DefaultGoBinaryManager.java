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

package com.github.blindpirate.gogradle.crossplatform;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.CompressUtils;
import com.github.blindpirate.gogradle.util.HttpUtils;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.zafarkhaja.semver.Version;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static com.github.blindpirate.gogradle.util.IOUtils.toRealPath;
import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult;
import static org.apache.commons.io.FileUtils.listFiles;

@Singleton
public class DefaultGoBinaryManager implements GoBinaryManager {
    private static final Version GO_VERSION_SUPPORTING_JSON = Version.valueOf("1.10.0");
    private static final Version GO_VET_IGNORING_VENDOR = Version.valueOf("1.9.0");

    private static final Logger LOGGER = Logging.getLogger(DefaultGoBinaryManager.class);

    public static final String FILENAME = "go${version}.${os}-${arch}${extension}";

    private static final String NEWEST_VERSION_URL
            = "http://gogradle.oss-cn-hongkong.aliyuncs.com/newest-stable-go-version.txt";

    // go version go1.7.1 darwin/amd64
    private static final Pattern GO_VERSION_OUTPUT_REGEX = Pattern.compile("go version go((\\d|\\.)+) .+");

    private static final String IGNORE_LOCAL = "IGNORE_LOCAL";

    private final GolangPluginSetting setting;
    private final GlobalCacheManager globalCacheManager;
    private final HttpUtils httpUtils;
    private final ProcessUtils processUtils;

    private boolean resolved = false;
    private Path binaryPath;
    private Path goroot;
    private String goVersionString;
    private Version goVersion;

    @Inject
    public DefaultGoBinaryManager(GolangPluginSetting setting,
                                  GlobalCacheManager globalCacheManager,
                                  HttpUtils httpUtils,
                                  ProcessUtils processUtils) {
        this.setting = setting;
        this.globalCacheManager = globalCacheManager;
        this.httpUtils = httpUtils;
        this.processUtils = processUtils;
    }

    @Override
    public Path getBinaryPath() {
        resolveIfNecessary();
        return binaryPath;
    }

    @Override
    public Path getGoroot() {
        resolveIfNecessary();
        return goroot;
    }

    @Override
    public boolean supportTestJsonOutput() {
        return goVersion.compareTo(GO_VERSION_SUPPORTING_JSON) >= 0;
    }

    @Override
    public boolean goVetIgnoreVendor() {
        return goVersion.compareTo(GO_VET_IGNORING_VENDOR) >= 0;
    }

    @Override
    public String getGoVersion() {
        resolveIfNecessary();
        return goVersionString;
    }

    private void resolveIfNecessary() {
        if (resolved) {
            return;
        }
        determineGoBinaryAndVersion();
        // $GOROOT/bin/go -> $GOROOT
        if (setting.getGoRoot() != null) {
            goroot = Paths.get(setting.getGoRoot());
        } else {
            goroot = toRealPath(binaryPath).resolve("../..").normalize();
        }
        resolved = true;
    }

    private void determineGoBinaryAndVersion() {
        if (IGNORE_LOCAL.equals(setting.getGoExecutable())) {
            fetchGoDistribution();
        } else if ("go".equals(setting.getGoExecutable())) {
            Optional<Pair<Path, String>> binPathAndVersionOnHost = findGoBinAndVersionHost();
            if (binPathAndVersionOnHost.isPresent()) {
                Path goBinPath = binPathAndVersionOnHost.get().getLeft();
                String version = binPathAndVersionOnHost.get().getRight();
                if (versionMatch(version)) {
                    useGoExecutableOnHost(goBinPath, version);
                } else {
                    fetchSpecifiedVersion(setting.getGoVersion());
                }
            } else {
                fetchGoDistribution();
            }
        } else {
            Optional<Pair<Path, String>> pathAndVersion = tryGivenGoExecutable();
            Assert.isTrue(pathAndVersion.isPresent(), "Cannot execute given go binary: " + setting.getGoExecutable());
            Assert.isTrue(versionMatch(pathAndVersion.get().getRight()),
                    "Version not match: required is " + setting.getGoVersion()
                            + ", given is " + pathAndVersion.get().getRight());
            useGoExecutableOnHost(pathAndVersion.get().getLeft(), pathAndVersion.get().getRight());
        }
    }

    private void fetchGoDistribution() {
        if (setting.getGoVersion() == null) {
            fetchNewestStableVersion();
        } else {
            fetchSpecifiedVersion(setting.getGoVersion());
        }
    }

    private boolean versionMatch(String actualVersion) {
        return setting.getGoVersion() == null || setting.getGoVersion().equals(actualVersion);
    }

    private void useGoExecutableOnHost(Path goBinPathOnHost, String versionOnHost) {
        LOGGER.quiet("Found go {} in {}, use it.", versionOnHost, goBinPathOnHost);
        binaryPath = goBinPathOnHost;
        setGoVersionString(versionOnHost);
    }

    private void setGoVersionString(String version) {
        goVersionString = version;
        goVersion = parseGoVersion(version);
    }

    private Version parseGoVersion(String versionString) {
        if (StringUtils.count(versionString, '.') == 1) {
            return Version.valueOf(versionString + ".0");
        } else {
            return Version.valueOf(versionString);
        }
    }

    private void fetchNewestStableVersion() {
        try {
            String newestStableVersion = httpUtils.get(NEWEST_VERSION_URL).trim();
            fetchSpecifiedVersion(newestStableVersion);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private Optional<Pair<Path, String>> tryGivenGoExecutable() {
        Path givenGoExecutablePath = Paths.get(setting.getGoExecutable());
        Optional<Pair<Path, String>> ret = tryInvokeGoVersion(givenGoExecutablePath);
        Assert.isTrue(ret.isPresent(), "Failed to run given go executable: " + setting.getGoExecutable());
        return ret;
    }

    @SuppressWarnings({"checkstyle:localvariablename"})
    private Optional<Pair<Path, String>> findGoBinAndVersionHost() {
        String PATH = System.getenv("PATH");
        String[] paths = StringUtils.splitAndTrim(PATH, File.pathSeparator);

        for (String path : paths) {
            Path goExecutablePath = Paths.get(path).resolve("go" + Os.getHostOs().exeExtension());
            Optional<Pair<Path, String>> pathAndVersion = tryInvokeGoVersion(goExecutablePath);
            if (pathAndVersion.isPresent()) {
                return pathAndVersion;
            }
        }
        return Optional.empty();
    }

    private Optional<Pair<Path, String>> tryInvokeGoVersion(Path executablePath) {
        try {
            Process process = processUtils.run(executablePath.toAbsolutePath().toString(), "version");
            ProcessResult result = processUtils.getResult(process);
            Matcher m = GO_VERSION_OUTPUT_REGEX.matcher(result.getStdout());
            if (m.find()) {
                Pair binaryPathAndVersion = Pair.of(executablePath, m.group(1));
                return Optional.of(binaryPathAndVersion);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.debug("Encountered exception when running go version via: " + executablePath.toAbsolutePath(), e);
            return Optional.empty();
        }
    }

    private void fetchSpecifiedVersion(String version) {
        goroot = globalCacheManager.getGlobalGoBinCacheDir(version).toPath().resolve("go");
        setGoVersionString(version);

        binaryPath = goroot.resolve("bin/go" + Os.getHostOs().exeExtension());
        if (!Files.exists(binaryPath)) {
            LOGGER.quiet("Start downloading go {}.", version);
            downloadSpecifiedVersion(version);
            addXPermissionToAllDescendant(goroot.resolve("bin"));
            addXPermissionToAllDescendant(goroot.resolve("pkg/tool"));
        }
    }

    private void addXPermissionToAllDescendant(Path path) {
        listFiles(path.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).stream()
                .map(File::toPath)
                .forEach(IOUtils::chmodAddX);
    }

    private void downloadSpecifiedVersion(String version) {
        Path archivePath = downloadArchive(version);
        CompressUtils.decompressZipOrTarGz(archivePath.toFile(),
                globalCacheManager.getGlobalGoBinCacheDir(version));
    }

    private Path downloadArchive(String version) {
        String url = injectVariables(setting.getGoBinaryDownloadTemplate(), version);
        String archiveFileName = injectVariables(FILENAME, version);
        File goBinaryCachePath = globalCacheManager.getGlobalGoBinCacheDir(archiveFileName);
        forceMkdir(goBinaryCachePath.getParentFile());
        LOGGER.warn("downloading {} -> {}", url, goBinaryCachePath.getAbsolutePath());
        try {
            httpUtils.download(url, goBinaryCachePath.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return goBinaryCachePath.toPath();
    }

    private String injectVariables(String template, String version) {
        String ret = template.replace("${version}", version);
        ret = ret.replace("${os}", Os.getHostOs().toString());
        ret = ret.replace("${arch}", Arch.getHostArch().toString());
        ret = ret.replace("${extension}", Os.getHostOs().archiveExtension());
        return ret;
    }
}
