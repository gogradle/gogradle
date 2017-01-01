package com.github.blindpirate.gogradle.core.dependency.produce.external.glide;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.util.Assert;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Model of glide.lock in repo managed by glide.
 *
 * @see <a href="https://github.com/Masterminds/glide/blob/master/glide.lock">glide.lock</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlideDotLockModel {
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("updated")
    private String updated;
    @JsonProperty("imports")
    private List<ImportBean> imports;
    @JsonProperty("testImport")
    private List<ImportBean> testImports;

    public List<Map<String, Object>> toNotations() {
        return imports.stream().map(ImportBean::toNotation).collect(Collectors.toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImportBean {
        @JsonProperty("name")
        String name;
        @JsonProperty("version")
        String version;

        public Map<String, Object> toNotation() {
            Assert.isNotBlank(name);
            Assert.isNotBlank(version);
            return ImmutableMap.of("name", name, "version", version);
        }
    }
}
