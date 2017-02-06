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
                    "version", revision);
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
