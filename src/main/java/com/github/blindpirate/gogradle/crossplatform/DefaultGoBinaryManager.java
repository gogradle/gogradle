package com.github.blindpirate.gogradle.crossplatform;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.util.CompressUtils;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.HttpUtils;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import static org.apache.commons.io.FileUtils.listFiles;

@Singleton
public class DefaultGoBinaryManager implements GoBinaryManager {
    private static final Logger LOGGER = Logging.getLogger(DefaultGoBinaryManager.class);
    // https://storage.googleapis.com/golang/go1.7.4.windows-386.zip
    // https://storage.googleapis.com/golang/go1.7.4.linux-amd64.tar.gz
    private static final String URL
            = "https://storage.googleapis.com/golang/go${version}.${os}-${arch}${extension}";
    // http://golangtc.com/static/go/1.7.4/go1.7.4.windows-386.zip
    // http://golangtc.com/static/go/1.7.4/go1.7.4.linux-amd64.tar.gz
    private static final String URL_UNDER_GFW
            = "http://golangtc.com/static/go/${version}/go${version}.${os}-${arch}${extension}";

    private static final String FILENAME = "go${version}.${os}-${arch}${extension}";

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
    private String goVersion;

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

    @Override
    public String getGoVersion() {
        resolveIfNecessary();
        return goVersion;
    }

    private void resolveIfNecessary() {
        if (resolved) {
            return;
        }
        determineGoBinaryAndVersion();
        resolved = true;
    }

    private void determineGoBinaryAndVersion() {
        Optional<String> versionOnHost = goVersionOnHost(setting.getGoExecutable());

        if (setting.getGoVersion() != null) {
            if (specificVersionIsHostVersion(versionOnHost)) {
                useGoExecutableOnHost(versionOnHost.get());
            } else {
                fetchSpecifiedVersion(setting.getGoVersion());
            }
        } else {
            if (versionOnHost.isPresent()) {
                useGoExecutableOnHost(versionOnHost.get());
            } else {
                fetchNewestStableVersion();
            }
        }
    }

    private void useGoExecutableOnHost(String versionOnHost) {
        LOGGER.quiet("Found go {}, use it.", versionOnHost);
        binaryPath = setting.getGoExecutable();
        goVersion = versionOnHost;
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

    private Optional<String> goVersionOnHost(String goExePath) {
        try {
            Process process = run(goExePath, "version");
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
        Path gorootPath = globalCacheManager.getGlobalGoBinCache(version).resolve("go");
        gorootEnv = gorootPath.toAbsolutePath().toString();
        goVersion = version;

        Path goExecutablePath = gorootPath.resolve("bin/go");
        binaryPath = goExecutablePath.toString();
        if (!Files.exists(goExecutablePath)) {
            LOGGER.quiet("Start downloading go {}.", version);
            downloadSpecifiedVersion(version);
            addXPermissionToAllDescendant(gorootPath.resolve("bin"));
            addXPermissionToAllDescendant(gorootPath.resolve("pkg/tool"));
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
                globalCacheManager.getGlobalGoBinCache(version).toFile());
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private Path downloadArchive(String version) {
        String baseUrl = urls.get(setting.isFuckGfw());
        String url = injectVariables(baseUrl, version);
        String archiveFileName = injectVariables(FILENAME, version);
        Path goBinaryCachePath = globalCacheManager.getGlobalGoBinCache(archiveFileName);
        IOUtils.forceMkdir(goBinaryCachePath.getParent().toFile());
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
