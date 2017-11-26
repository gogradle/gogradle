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

package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.GolangRepository;
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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

//https://golang.org/cmd/go/#hdr-Remote_import_paths
@Singleton
public class MetadataPackagePathResolver implements PackagePathResolver {
    public static final String GO_USER_AGENT = "Go-http-client/1.1";
    private static final Logger LOGGER = Logging.getLogger(MetadataPackagePathResolver.class);

    private final HttpUtils httpUtils;

    @Inject
    public MetadataPackagePathResolver(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        if (GogradleGlobal.isOffline()) {
            LOGGER.info("Fetching metadata of {} is skipped since it is offline now.", packagePath);
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

            Optional<GoImportMetaTag> metaTag = findMatchedMetaTag(packagePath, url, elements);
            if (!metaTag.isPresent()) {
                LOGGER.debug("Cannot find matched meta tag in response of {}", url);
                return Optional.empty();
            }
            return Optional.of(buildPackageInfo(packagePath, metaTag.get()));
        } catch (IOException e) {
            LOGGER.info("Exception in accessing {}", url, e);
            return Optional.empty();
        }

    }

    private Optional<GoImportMetaTag> findMatchedMetaTag(String packagePath, String url, Elements elements) {
        return elements.stream()
                .map(element -> new GoImportMetaTag(element.attr("content")))
                .filter(tag -> packagePath.startsWith(tag.rootPath))
                .findFirst();
    }

    private GolangPackage buildPackageInfo(String packagePath, GoImportMetaTag metaTag) {
        GolangRepository repository = GolangRepository.newOriginalRepository(metaTag.vcs, metaTag.repoUrl);
        return VcsGolangPackage.builder()
                .withPath(Paths.get(packagePath))
                .withRepository(repository)
                .withRootPath(Paths.get(metaTag.rootPath))
                .build();
    }

    private String fetchHtml(String url) throws IOException {
        String realUrl = httpUtils.appendQueryParams(url, ImmutableMap.of("go-get", "1"));
        Map<String, String> headers = ImmutableMap.of(HttpUtils.USER_AGENT, GO_USER_AGENT);
        return httpUtils.get(realUrl, headers);
    }


    static class GoImportMetaTag {
        private String rootPath;
        private VcsType vcs;
        private String repoUrl;

        GoImportMetaTag(String content) {
            String[] array = StringUtils.splitAndTrim(content, " ");
            Assert.isTrue(array.length > 2, "Invalid content:" + content);
            rootPath = array[0];
            vcs = VcsType.of(array[1]).get();
            repoUrl = array[2];
        }
    }

}
