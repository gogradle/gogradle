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

package com.github.blindpirate.gogradle.core.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.common.WithApiVersion;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.pack.MetadataPackagePathResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Stands for the metadata of a repository.
 */
public class GlobalCacheMetadata extends WithApiVersion {
    /**
     * "Original" means the urls specified in go-import meta tag of HTML.
     *
     * @see MetadataPackagePathResolver
     */
    @JsonProperty("originalUrls")
    private List<String> originalUrls = new ArrayList<>();

    /**
     * The last update time and url.
     */
    @JsonProperty("lastUpdated")
    private LastUpdated lastUpdated;

    /**
     * "Original" means the vcs type specified in go-import meta tag of HTML.
     *
     * @see MetadataPackagePathResolver
     */
    @JsonProperty("originalVcs")
    private String originalVcs;

    public String getOriginalVcs() {
        return originalVcs;
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
        if (pkg.getOriginalVcsInfo() != null) {
            metadata.originalUrls = pkg.getOriginalVcsInfo().getUrls();
            metadata.originalVcs = pkg.getOriginalVcsInfo().getVcsType().getName();
        }
        metadata.lastUpdated = new LastUpdated();
        return metadata;
    }

    public void update(String currentUrl) {
        lastUpdated = new LastUpdated();
        lastUpdated.time = System.currentTimeMillis();
        lastUpdated.url = currentUrl;
    }

    private static class LastUpdated {
        @JsonProperty("time")
        private long time;
        @JsonProperty("url")
        private String url;
    }
}
