package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.pack.PackageResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.HttpUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

// import "example.org/pkg/foo"
// will result in the following requests:
//
// https://example.org/pkg/foo?go-get=1 (preferred)
// http://example.org/pkg/foo?go-get=1  (fallback, only with -insecure)
// If that page contains the meta tag
//
// <meta name="go-import" content="example.org git https://code.org/r/p/exproj">
// the go tool will verify that https://example.org/?go-get=1 contains the same meta tag and
// then git clone https://code.org/r/p/exproj into GOPATH/src/example.org.
// TODO the verification not implemented yet
public class GoImportMetadataFetcher implements PackageFetcher {
    @Inject
    private HttpUtils httpUtils;

    @Override
    public void fetch(String packageName, Path location) {
        try {
            fetchViaWeb(HTTPS + packageName, location);
        } catch (Throwable e) {
            e.printStackTrace();
            fetchViaWeb(HTTP + packageName, location);
        }
    }

    private void fetchViaWeb(String url, Path location) {
        try {
            String html = httpUtils.get(url, ImmutableMap.of("go-get", "1"));

            Document document = Jsoup.parse(html);
            Elements elements = document.select("meta[name=go-import]");
            if (elements.isEmpty()) {
                throw new IllegalStateException("Can not find go-import meta tag in:" + url);
            } else {
                String content = elements.get(0).attr("content");
                // TODO According to https://golang.org/cmd/go/#hdr-Remote_import_paths
                // we should verify it
                //String importPath = matcher.group(1);
                VcsType vcs = extractVcs(content);
                String realUrl = extractRealUrl(content);
                vcs.getFetcher().fetch(realUrl, location);
            }
        } catch (IOException e) {
            throw new PackageResolutionException("Error in access:" + url);
        }
    }

    private String extractRealUrl(String content) {
        String[] array = StringUtils.splitAndTrim(content, " ");
        Assert.isTrue(array.length > 2, "Invalid content:" + content);
        return array[2];
    }

    private VcsType extractVcs(String content) {
        String[] array = StringUtils.splitAndTrim(content, " ");
        Assert.isTrue(array.length > 2, "Invalid content:" + content);
        Optional<VcsType> vcs = VcsType.of(array[1]);
        Assert.isTrue(vcs.isPresent(), "Invalid content:" + content);
        return vcs.get();
    }

}
