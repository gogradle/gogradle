package com.github.blindpirate.gogradle.core.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.common.WithApiVersion;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;

import java.util.List;

public class GlobalCacheMetadata extends WithApiVersion {
    @JsonProperty("originalUrls")
    private List<String> originalUrls;

    @JsonProperty("lastUpdated")
    private LastUpdated lastUpdated;

    @JsonProperty("vcs")
    private String vcs;

    public String getVcs() {
        return vcs;
    }

    public List<String> getOriginalUrls() {
        return originalUrls;
    }

    @JsonIgnore
    public String getLastUpdateUrl() {
        return lastUpdated.url;
    }

    @JsonIgnore
    public long getLastUpdateTime() {
        return lastUpdated.time;
    }

    public static GlobalCacheMetadata newMetadata(VcsGolangPackage pkg) {
        GlobalCacheMetadata metadata = new GlobalCacheMetadata();
        metadata.originalUrls = pkg.getUrls();
        metadata.vcs = pkg.getVcsType().getName();
        metadata.lastUpdated = new LastUpdated();
        return metadata;
    }

    public static GlobalCacheMetadata updatedMetadata(VcsGolangPackage pkg, String currentUrl) {
        GlobalCacheMetadata metadata = new GlobalCacheMetadata();
        metadata.originalUrls = pkg.getUrls();
        metadata.vcs = pkg.getVcsType().getName();
        metadata.lastUpdated = new LastUpdated();
        metadata.lastUpdated.time = System.currentTimeMillis();
        metadata.lastUpdated.url = currentUrl;
        return metadata;
    }

    private static class LastUpdated {
        @JsonProperty("time")
        private long time;
        @JsonProperty("url")
        private String url;
    }
}
