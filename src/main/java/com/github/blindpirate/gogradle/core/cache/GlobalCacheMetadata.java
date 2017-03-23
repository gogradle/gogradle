package com.github.blindpirate.gogradle.core.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.common.WithApiVersion;

import java.util.List;

public class GlobalCacheMetadata extends WithApiVersion {
    @JsonProperty("originalUrls")
    private List<String> originalUrls;

    @JsonProperty("lastUpdated")
    private LastUpdated lastUpdated;

    public List<String> getOriginalUrls() {
        return originalUrls;
    }

    private static class LastUpdated {
        private long time;
        private String url;
    }
}
