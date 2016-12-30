package com.github.blindpirate.gogradle.core.dependency.external.godep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public String getImportPath() {
        return ImportPath;
    }

    public String getGoVersion() {
        return GoVersion;
    }

    public String getGodepVersion() {
        return GodepVersion;
    }

    public List<DepsBean> getDeps() {
        return Deps;
    }

    public static class DepsBean {
        @JsonProperty("ImportPath")
        private String ImportPath;
        @JsonProperty("Rev")
        private String Rev;
        @JsonProperty("Comment")
        private String Comment;

        public Map<String, Object> toNotation() {
            return ImmutableMap.<String, Object>of(
                    "name", ImportPath,
                    "commit", Rev);
        }

        public String getImportPath() {
            return ImportPath;
        }

        public String getRev() {
            return Rev;
        }

        public String getComment() {
            return Comment;
        }
    }
}
