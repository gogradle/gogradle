package com.github.blindpirate.gogradle.crossplatform;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.util.CompressUtils;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.HttpUtils;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessResult;
import static com.github.blindpirate.gogradle.util.ProcessUtils.getResult;
import static com.github.blindpirate.gogradle.util.ProcessUtils.run;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Singleton
public class DefaultGoBinaryManager implements GoBinaryManager {
    // https://storage.googleapis.com/golang/go1.7.4.windows-386.zip
    // https://storage.googleapis.com/golang/go1.7.4.linux-amd64.tar.gz
    private static final String URL
            = "https://storage.googleapis.com/golang/go${version}.${os}-${arch}${extension}";
    // http://golangtc.com/static/go/1.7.4/go1.7.4.windows-386.zip
    // http://golangtc.com/static/go/1.7.4/go1.7.4.linux-amd64.tar.gz
    private static final String URL_UNDER_GFW
            = "http://golangtc.com/static/go/${version}/go${version}.${os}-${arch}${extension}";

    private static final String FILENAME = "go${version}.${os}-${arch}${extension}";

    private static final String GOBIN_RELATIVE = "go/bin/go";

    private static final String NEWEST_VERSION_URL
            = "http://gogradle.oss-cn-hongkong.aliyuncs.com/newest-stable-go-version.txt";

    // go version go1.7.1 darwin/amd64
    private static final Pattern GO_VERSION_OUTPUT_REGEX = Pattern.compile("go version go((\\d|\\.)+) .+");

    private static Map<Boolean, String> urls = ImmutableMap.of(
            TRUE, URL_UNDER_GFW,
            FALSE, URL);


    private final GolangPluginSetting setting;
    private final GlobalCacheManager globalCacheManager;
    private final HttpUtils httpUtils;

    private boolean resolved = false;
    private String binaryPath;
    private String gorootEnv;

    @Inject
    public DefaultGoBinaryManager(GolangPluginSetting setting,
                                  GlobalCacheManager globalCacheManager,
                                  HttpUtils httpUtils) {
        this.setting = setting;
        this.globalCacheManager = globalCacheManager;
        this.httpUtils = httpUtils;
    }

    @Override
    public String getBinaryPath() {
        resolveIfNecessary();
        return binaryPath;
    }

    @Override
    public String getGorootEnv() {
        resolveIfNecessary();
        return gorootEnv;
    }

    private void resolveIfNecessary() {
        if (resolved) {
            return;
        }
        if (setting.getGoExecutable() != null) {
            binaryPath = setting.getGoExecutable();
        } else {
            determineGoBinary();
        }
        resolved = true;
    }

    void determineGoBinary() {
        Optional<String> versionOnHost = goVersionOnHost();

        if (setting.getGoVersion() != null) {
            if (specificVersionIsHostVersion(versionOnHost)) {
                binaryPath = "go";
            } else {
                fetchSpecifiedVersion(setting.getGoVersion());
            }
        } else {
            if (versionOnHost.isPresent()) {
                binaryPath = "go";
            } else {
                fetchNewestStableVersion();
            }
        }
    }

    private boolean specificVersionIsHostVersion(Optional<String> versionOnHost) {
        return setting.getGoVersion().equals(versionOnHost.orElse(null));
    }

    private void fetchNewestStableVersion() {
        try {
            String newestStableVersion = httpUtils.get(NEWEST_VERSION_URL).trim();
            fetchSpecifiedVersion(newestStableVersion);
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private Optional<String> goVersionOnHost() {
        try {
            Process process = run("go", "version");
            ProcessResult result = getResult(process);
            Matcher m = GO_VERSION_OUTPUT_REGEX.matcher(result.getStdout());
            if (m.find()) {
                return Optional.of(m.group(1));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void fetchSpecifiedVersion(String version) {
        Path goBinaryPath = globalCacheManager
                .getGlobalGoBinCache(version)
                .resolve(GOBIN_RELATIVE);
        binaryPath = goBinaryPath.toAbsolutePath().toString();
        if (!goBinaryPath.toFile().exists()) {
            downloadSpecifiedVersion(version);
        }
    }

    private void downloadSpecifiedVersion(String version) {
        Path goRootPath = globalCacheManager.getGlobalGoBinCache(version);
        Path goBinaryPath = goRootPath.resolve(GOBIN_RELATIVE);
        Path archivePath = downloadArchive(version);
        CompressUtils.decompressZipOrTarGz(archivePath.toFile(), goRootPath.toFile());

        IOUtils.chmodAddX(goBinaryPath);

        gorootEnv = goRootPath.toAbsolutePath().toString();
    }

    private Path downloadArchive(String version) {
        String baseUrl = urls.get(setting.isFuckGfw());
        String url = injectVariables(baseUrl, version);
        String archiveFileName = injectVariables(FILENAME, version);
        Path goBinaryCachePath = globalCacheManager.getGlobalGoBinCache(archiveFileName);
        try {
            httpUtils.download(url, goBinaryCachePath);
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
        return goBinaryCachePath;
    }

    private String injectVariables(String template, String version) {
        String ret = template.replace("${version}", version);
        ret = ret.replace("${os}", Os.getHostOs().toString());
        ret = ret.replace("${arch}", Arch.getHostArch().toString());
        ret = ret.replace("${extension}", Os.getHostOs().archiveExtension());
        return ret;
    }
}
