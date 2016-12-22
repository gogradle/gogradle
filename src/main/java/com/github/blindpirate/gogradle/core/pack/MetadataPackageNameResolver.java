package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.HttpUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class MetadataPackageNameResolver implements PackageNameResolver {
    private static final Logger LOGGER = Logging.getLogger(MetadataPackageNameResolver.class);

    private final HttpUtils httpUtils;

    @Inject
    public MetadataPackageNameResolver(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    @Override
    public Optional<PackageInfo> produce(String packageName) {
        Optional<PackageInfo> httpsResult = fetchViaWeb(packageName, HTTPS + packageName);
        if (httpsResult.isPresent()) {
            return httpsResult;
        } else {
            return fetchViaWeb(packageName, HTTP + packageName);
        }
    }

    private Optional<PackageInfo> fetchViaWeb(String packageName, String url) {
        try {
            String html = fetchHtml(url);

            Document document = Jsoup.parse(html);
            Elements elements = document.select("meta[name=go-import]");
            if (elements.isEmpty()) {
                LOGGER.info("Missing meta in response of {}", url);
                return Optional.absent();
            } else {
                String content = elements.get(0).attr("content");
                // TODO According to https://golang.org/cmd/go/#hdr-Remote_import_paths
                // we should verify it
                //String importPath = matcher.group(1);
                LOGGER.info("Meta tag of url {} is:{}", url, content);
                return Optional.of(buildPackageInfo(packageName, content));
            }
        } catch (IOException e) {
            LOGGER.warn("Exception in accessing {}", url, e);
            return Optional.absent();
        }

    }

    private PackageInfo buildPackageInfo(String packageName, String content) {
        String[] array = StringUtils.splitAndTrim(content, " ");
        Assert.isTrue(array.length > 2, "Invalid content:" + content);
        String rootName = array[0];
        VcsType vcs = VcsType.of(array[1]).get();
        String url = array[2];

        return PackageInfo.builder()
                .withName(packageName)
                .withVcsType(vcs)
                .withRootName(rootName)
                .withUrl(url)
                .build();
    }

    private String fetchHtml(String url) throws IOException {
        String realUrl = httpUtils.appendQueryParams(url, ImmutableMap.of("go-get", "1"));
        return httpUtils.get(realUrl);
    }

}
