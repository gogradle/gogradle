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

package com.github.blindpirate.gogradle.core.dependency.produce.external.glide;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Model of glide.lock in repo managed by glide.
 *
 * @see <a href="https://github.com/Masterminds/glide/blob/master/glide.lock">glide.lock</a>
 * @see <a href="https://github.com/Masterminds/glide/blob/master/docs/glide.yaml.md">glide.yaml.md</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlideDotLockModel {
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("updated")
    private String updated;
    @JsonProperty("imports")
    private List<ImportBean> imports;
    @JsonProperty("testImports")
    private List<ImportBean> testImports;

    public List<Map<String, Object>> toBuildNotations() {
        if (imports == null) {
            return Collections.emptyList();
        }
        return imports.stream().map(ImportBean::toNotation).collect(Collectors.toList());
    }

    public List<Map<String, Object>> toTestNotations() {
        if (testImports == null) {
            return Collections.emptyList();
        }
        return testImports.stream().map(ImportBean::toNotation).collect(Collectors.toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImportBean {
        @JsonProperty("name")
        private String name;
        @JsonProperty("version")
        private String version;
        @JsonProperty("vcs")
        private String vcs;
        @JsonProperty("repo")
        private String repo;
        @JsonProperty("subpackages")
        private List<String> subpackages;

        public Map<String, Object> toNotation() {
            Assert.isNotBlank(name);

            Map<String, Object> ret = MapUtils.asMapWithoutNull(
                    "name", name,
                    "version", version,
                    "url", repo,
                    "vcs", vcs,
                    "transitive", false);
            if (subpackages != null) {
                // It seems that subpackages in glide contains files in 'root' dir
                // So here we act as an adapter
                Set<String> realSubpackages = Sets.newHashSet(GolangDependency.ONLY_CURRENT_FILES);
                realSubpackages.addAll(subpackages);
                ret.put("subpackages", realSubpackages);
            }
            return ret;
        }
    }
}
