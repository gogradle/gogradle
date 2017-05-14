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

package com.github.blindpirate.gogradle.core.dependency.produce.external.gvtgbvendor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.StringUtils.isBlank;
import static com.github.blindpirate.gogradle.util.StringUtils.removeEnd;

/**
 * Model of vendor/manifest in repos managed by gvt or gbvendor.
 *
 * @see <a href="https://github.com/FiloSottile/gvt/blob/master/vendor/manifest">manifest</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManifestModel {

    @JsonProperty("generator")
    private String generator;
    @JsonProperty("dependencies")
    private List<DependenciesBean> dependencies;

    public List<Map<String, Object>> toNotations() {
        return dependencies.stream().map(DependenciesBean::toNotation).collect(Collectors.toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DependenciesBean {
        @JsonProperty("importpath")
        private String importpath;
        @JsonProperty("repository")
        private String repository;
        @JsonProperty("vcs")
        private String vcs;
        @JsonProperty("revision")
        private String revision;
        @JsonProperty("branch")
        private String branch;
        @JsonProperty("path")
        private String path;
        @JsonProperty("notests")
        private boolean notests;

        public Map<String, Object> toNotation() {
            Assert.isNotBlank(importpath);
            return MapUtils.asMapWithoutNull("vcs", vcs,
                    "name", determineName(),
                    "version", revision,
                    "transitive", false);
        }

        private String determineName() {
            if (isBlank(path)) {
                return importpath;
            } else {
                Assert.isTrue(importpath.endsWith(path));
                return removeEnd(importpath, path);
            }
        }
    }
}
