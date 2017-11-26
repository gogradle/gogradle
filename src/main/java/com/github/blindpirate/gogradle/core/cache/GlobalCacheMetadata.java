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
import com.github.blindpirate.gogradle.core.GolangRepository;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Stands for the metadata of a package.
 * <pre>
 *     {@code
 *     ---
 *     apiVersion: "0.8.0"
 *     package: "github.com/golang/tools
 *     repositories:
 *     - vcs: "git"
 *       urls:
 *       - "https://github.com/golang/tools.git"
 *       - "git@github.com:golang/tools.git"
 *       lastUpdatedTime: 1491460172634
 *       dir: 1a2b3c4d
 *       original: true
 *     }
 * </pre>
 */
public class GlobalCacheMetadata extends WithApiVersion {

    @JsonProperty("package")
    private String pkg;

    @JsonProperty("repositories")
    private List<GolangRepositoryMetadata> repositories;

    @JsonIgnore
    private boolean dirty;

    public static GlobalCacheMetadata newMetadata(VcsGolangPackage pkg) {
        GlobalCacheMetadata metadata = new GlobalCacheMetadata();
        metadata.pkg = pkg.getRootPathString();
        metadata.repositories = new ArrayList<>();
        metadata.repositories.add(new GolangRepositoryMetadata(pkg.getRepository()));
        metadata.dirty = true;
        return metadata;
    }

    public List<GolangRepositoryMetadata> getRepositories() {
        return repositories;
    }

    public void addRepository(GolangRepository repository) {
        dirty = true;
        repositories.add(new GolangRepositoryMetadata(repository));
    }

    public boolean isDirty() {
        return dirty;
    }

    public static class GolangRepositoryMetadata extends GolangRepository {
        /**
         * The last update time.
         */
        @JsonProperty("lastUpdatedTime")
        private long lastUpdatedTime;

        /**
         * The directory name where this repository locates.
         */
        @JsonProperty("dir")
        private String dir;

        private GolangRepositoryMetadata(GolangRepository repository) {
            this.urls = repository.getUrls();
            this.dir= getUrlHash();
            this.original = repository.isOriginal();
            this.vcs = repository.getVcsType();
        }

        public long getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public void updated() {
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public String getDir() {
            return dir;
        }

        private String getUrlHash() {
            String combined = CollectionUtils.toSorted(getUrls()).stream()
                    .collect(StringBuilder::new, StringBuilder::append, null).toString();
            return DigestUtils.md5Hex(combined);
        }


    }

//
//
//    public static GlobalCacheMetadata newMetadata(VcsGolangPackage pkg) {
//        GlobalCacheMetadata metadata = new GlobalCacheMetadata();
//        if (pkg.getOriginalVcsInfo() != null) {
//            metadata.originalUrls = pkg.getOriginalVcsInfo().getUrls();
//            metadata.originalVcs = pkg.getOriginalVcsInfo().getVcsType().getName();
//        }
//        metadata.lastUpdated = new LastUpdated();
//        return metadata;
//    }
//
//    public void update(String currentUrl) {
//        lastUpdated = new LastUpdated();
//        lastUpdated.time = System.currentTimeMillis();
//        lastUpdated.url = currentUrl;
//    }


}
