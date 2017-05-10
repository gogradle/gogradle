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
            VersionConverter.determineVersionAndPutIntoMap(ret, version);
            return ret;
        }
    }
}
