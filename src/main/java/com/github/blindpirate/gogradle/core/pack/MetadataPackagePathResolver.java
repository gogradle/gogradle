package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.HttpUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class MetadataPackagePathResolver implements PackagePathResolver {
    private static final Logger LOGGER = Logging.getLogger(MetadataPackagePathResolver.class);

    private final HttpUtils httpUtils;

    @Inject
    public MetadataPackagePathResolver(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        if (InjectionHelper.isOffline()) {
            LOGGER.debug("Fetching metadata of {} is skipped since it is offline now.", packagePath);
            return Optional.empty();
        }
        Optional<GolangPackage> httpsResult = fetchViaWeb(packagePath, HTTPS + packagePath);
        if (httpsResult.isPresent()) {
            return httpsResult;
        } else {
            return fetchViaWeb(packagePath, HTTP + packagePath);
        }
    }

    private Optional<GolangPackage> fetchViaWeb(String packagePath, String url) {
        try {
            String html = fetchHtml(url);

            Document document = Jsoup.parse(html);
            Elements elements = document.select("meta[name=go-import]");
            Assert.isTrue(!elements.isEmpty(), "Missing meta in response of " + url);

            String content = elements.get(0).attr("content");
            LOGGER.info("Meta tag of url {} is:{}", url, content);
            return Optional.of(buildPackageInfo(packagePath, content));
        } catch (IOException e) {
            LOGGER.warn("Exception in accessing {}", url, e);
            return Optional.empty();
        }

    }

    private GolangPackage buildPackageInfo(String packagePath, String content) {
        String[] array = StringUtils.splitAndTrim(content, " ");
        Assert.isTrue(array.length > 2, "Invalid content:" + content);
        String rootPath = array[0];
        VcsType vcs = VcsType.of(array[1]).get();
        String url = array[2];

        return VcsGolangPackage.builder()
                .withPath(packagePath)
                .withVcsType(vcs)
                .withRootPath(rootPath)
                .withUrl(url)
                .build();
    }

    private String fetchHtml(String url) throws IOException {
        String realUrl = httpUtils.appendQueryParams(url, ImmutableMap.of("go-get", "1"));
        return httpUtils.get(realUrl);
    }

}
