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

package com.github.blindpirate.gogradle.core.dependency.produce.external.godep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Model of Godeps/Godeps.json.
 *
 * @see <a href="https://github.com/tools/godep/blob/master/Godeps/Godeps.json">Godeps.json</a>
 */
// NOTE: dependency in Godeps.json may be not the root package
// Godeps/Godeps.json
@SuppressWarnings({"checkstyle:membername", "checkstyle:parametername"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GodepsDotJsonModel {
    @JsonProperty("ImportPath")
    private String ImportPath;
    @JsonProperty("GoVersion")
    private String GoVersion;
    @JsonProperty("GodepVersion")
    private String GodepVersion;
    @JsonProperty("Deps")
    private List<DepsBean> Deps;

    public List<Map<String, Object>> toNotations() {
        return Deps.stream().map(DepsBean::toNotation).collect(Collectors.toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepsBean {
        @JsonProperty("ImportPath")
        private String ImportPath;
        @JsonProperty("Rev")
        private String Rev;
        @JsonProperty("Comment")
        private String Comment;

        public Map<String, Object> toNotation() {
            Assert.isNotBlank(ImportPath, "ImportPath cannot be blank!");
            return MapUtils.asMapWithoutNull(
                    "name", ImportPath,
                    "version", Rev,
                    "transitive", false);
        }
    }
}
