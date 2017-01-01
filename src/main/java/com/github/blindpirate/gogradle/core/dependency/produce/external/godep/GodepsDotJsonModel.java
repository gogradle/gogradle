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
 * @see <a href="https://github.com/tools/godep/blob/master/Godeps/Godeps.json">Godeps.json</a>.
 */
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
                    "version", Rev);
        }
    }
}
