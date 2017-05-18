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

package com.github.blindpirate.gogradle.core.dependency.produce.external.trash;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDotConfYamlModel {
    @JsonProperty("import")
    private List<ImportBean> importBeans;

    public List<Map<String, Object>> toBuildNotations() {
        return importBeans.stream().map(ImportBean::toNotation).collect(Collectors.toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImportBean {
        @JsonProperty("package")
        private String packageX;
        @JsonProperty("version")
        private String version;
        @JsonProperty("repo")
        private String repo;

        public Map<String, Object> toNotation() {
            Assert.isNotBlank(packageX);
            Map<String, Object> ret = MapUtils.asMapWithoutNull(
                    "name", packageX,
                    "url", repo,
                    "transitive", false);
            SimpleConfFileHelper.determineVersionAndPutIntoMap(ret, version);
            return ret;
        }
    }
}
